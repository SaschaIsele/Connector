# Hashicorp Vault authentication refactor

## Decision

Refactor the HashiCorp Vault extension and make it extensible with different methods of authentication.

## Rationale

The current implementation of the authentication in the HashiCorp Vault extension has no way to add new authentication methods and to switch between them.
The full list of possible authentication methods can be found in the [HashiCorp Vault Documentation](https://developer.hashicorp.com/vault/docs/auth)
Relevant examples are:

* [Token auth](https://developer.hashicorp.com/vault/docs/auth/token)
* [Kubernetes auth](https://developer.hashicorp.com/vault/docs/auth/kubernetes)
* [AppRole auth](https://developer.hashicorp.com/vault/docs/auth/approle)
It goes against the EDC extensibility model and needs to be remedied.
Additionally, extracting the current implementation of the token authentication into its own extension will improve readability and maintainability of the code.

## Approach
The refactor will affect only the `vault-hashicorp` extension.

To allow an extensible authentication module for HashiCorp Vault, the implementation will follow the registry pattern.

### Vault Auth SPI

To allow the proper organisation of the interfaces needed for the refactoring, a new module named `vault-auth-spi` is introduced in the `spi` directory.

### Vault Authentication Service Interface

To interact with a multitude of different authentication methods, an interface for the Vault Authentication Service is needed.
This interface will contain two methods:

* `login()`
* `getVaultToken()`

`login()` contains the authentication and fetches/generates the `client_token` from HashiCorp Vault.

`getVaultToken()` returns the saved `client_token` when needed.

```java
public interface HashicorpVaultAuth {

    void login();
    
    String getVaultToken();
}
```

### Vault Authentication Service Implementation

The Vault Authentication Service Implementations each implement a single HashiCorp Vault Authentication method.
To achieve this, each extension contains an implementation of the newly introduced Vault Authentication Service interface.
The goal of each extension is to authenticate with HashiCorp Vault and to provide a valid `client_token` to the `HashicorpVaultClient`.

Interface Implementation:

```java
public record HashicorpVaultTokenAuth(@NotNull String vaultToken) implements HashicorpVaultAuth {

    private String vaultToken;
    
    @Override
    public void login() {
        // no login is needed for token authentication
    }

    @Override
    public String getVaultToken() {
        return vaultToken;
    }
}
```

Simple example Token Auth Service Implementation:

```java
@Extension(value = HashicorpVaultTokenAuthExtension.NAME)
public class HashicorpVaultTokenAuthExtension implements ServiceExtension {

    @Setting(value = "The token used to access the Hashicorp Vault", required = true)
    public static final String VAULT_TOKEN = "edc.vault.hashicorp.token";
    
    public static final String NAME = "Vault Token Auth";

    @Inject
    private VaultAuthenticationRegistry vaultAuthenticationRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var token = context.getSetting(VAULT_TOKEN, null);
        
        if (!vaultAuthenticationRegistry.hasService("token-based")) {
            vaultAuthenticationRegistry.register("token-based", HashicorpVaultTokenAuth(token));
        }
 
    }
    
}
```

### Registry

Interface:

```java
public interface VaultAuthenticationRegistry {
    
    void register(String method, HashicorpVaultAuth authImplementation);
    
    @NotNull
    HashicorpVaultAuth resolve(String method);
    
    boolean hasService(String method);
}
```

Implementation:

```java
public class VaultAuthenticationRegistryImpl implements VaultAuthenticationRegistry {
    
    private final Map<String, HashicorpVaultAuth> services = new HashMap<>();

    public VaultAuthenticationRegistryImpl() {
    }

    @Override
    public void register(String context, HashicorpVaultAuth service) {
        services.put(method, service);
    }

    @Override
    public @NotNull HashicorpVaultAuth resolve(String method) {
        return services.get(method);
    }

    @Override
    public boolean hasService(String method) {
        return services.containsKey(method);
    }
}
```

### Registration

Each Service Implementation will register itself in the `VaultAuthenticationRegistry`.
This is done through an `@Provider` method inside the HashiCorp Vault extension, that is then injected with `@Inject` into the Service Implementation.

```java
@Provider
public VaultAuthenticationRegistry vaultAuthenticationRegistry() {
    return new VaultAuthenticationRegistryImpl();
}
```

The `VaultAuthenticationRegistry` is injected into the Vault Authentication Service Implementations through `@Inject`.

```java
@Inject
private VaultAuthenticationRegistry vaultAuthenticationRegistry;
```

It will then be registered during initialization through:

```java
@Override
public void initialize(ServiceExtensionContext context) {
    var token = context.getSetting(VAULT_TOKEN, null);

    if (!vaultAuthenticationRegistry.hasService("token-based")) {
        vaultAuthenticationRegistry.register("token-based", HashicorpVaultTokenAuth(token));
    }

}
```

### Configuration

A new config value is introduced to the HashiCorp Vault Extension named `edc.vault.hashicorp.auth.method`. This value governs which Vault Authentication Service Extension is used from the Vault Auth Registry.

Due to the possible differences in the configuration of each authentication method, each Vault Authentication Service Extension comes with its own configuration values.

### HashiCorp Vault Client

Since the client contains a lot of authentication logic, it will also go through a refactoring.
After the refactoring it will only use the `VaultAuthenticationRegistry` based on the configured `edc.vault.hashicorp.auth.method` to fetch the `client_token` that is provided by the Vault Authentication Service Extensions.
The `client_token` will then be used to generate the Headers for the HTTP requests.

Old `getHeaders()`:

```java
@NotNull
private Headers getHeaders() {
    var headersBuilder = new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true));
    headersBuilder.add(VAULT_TOKEN_HEADER, settings.token());
    return headersBuilder.build();
}
```

Example for new `getHeaders()`:
```java
@NotNull
private Headers getHeaders() {
    var headersBuilder = new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true));
    headersBuilder.add(VAULT_TOKEN_HEADER, vaultAuthenticationRegistry.resolve(settings.getAuthMethod()).getVaultToken());
    return headersBuilder.build();
}
```

### HashiCorp Vault Token Authentication Extension

The current token based authentication is refactored and moved into its own extension named `Vault Token Auth`.
This includes the token refresh functionality and will lead to a refactoring of the following classes:

* `HashicorpVaultExtension.java`
* `HashicorpVaultClient.java`
* `HashicorpVaultSettings.java`
* `HashicorpVaultTokenRenewTask.java`
