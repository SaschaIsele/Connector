# Hashicorp Vault authentication refactor

## Decision

Refactor the HashiCorp Vault extension and make it extensible with different methods of authentication.

## Rationale

The current implementation of the authentication in the HashiCorp Vault extension has no way to add new authentication methods.
The full list of possible authentication methods can be found in the [HashiCorp Vault Documentation](https://developer.hashicorp.com/vault/docs/auth)
Relevant examples are:

* [Token auth](https://developer.hashicorp.com/vault/docs/auth/token)
* [Kubernetes auth](https://developer.hashicorp.com/vault/docs/auth/kubernetes)
* [AppRole auth](https://developer.hashicorp.com/vault/docs/auth/approle)

It goes against the EDC extensibility model and needs to be remedied.
Additionally, extracting the current implementation of the token authentication into its own extension will improve readability and maintainability of the code.

## Approach

The refactor will affect only the `vault-hashicorp` extension.

To allow an extensible authentication for HashiCorp Vault, the implementation will follow the registry pattern.

### Hashicorp Vault Auth SPI

For the proper organisation of the interfaces needed for the refactoring, a new module named `hashicorp-vault-auth-spi` is introduced in the `spi` directory.

It will contain the following interfaces:

* [Hashicorp Vault Auth Interface](#hashicorp-vault-auth-interface)
* [Hashicorp Vault Auth Registry Interface](#hashicorp-vault-auth-registry)

### Hashicorp Vault Auth Interface

To implement a multitude of different authentication methods, an interface for the Hashicorp Vault authentication is needed.

```java
public interface HashicorpVaultAuth {

    // Contains the authentication logic and generates/stores the `client_token` from HashiCorp Vault in accordance to the chosen auth method.
    void login();
    
    // The stored token is returned.
    String vaultToken();
    
}
```

`HashicorpVaultAuth` implementations will be registered in `HashicorpVaultAuthRegistry` and used by the `HashicorpVaultClient` to receive the `client_token` for the request authentication.
More on that in the sections [Hashicorp Vault Auth Implementation Registration](#Hashicorp-Vault-Auth-Registration) and [HashiCorp Vault Client](#HashiCorp-Vault-Client)

### Haschicorp Vault Auth Service Extension

For every authentication method, a [Hashicorp Vault Auth Implementation](#Hashicorp-Vault-Auth-Implementation) of the [Hashicorp Vault Auth interface](#hashicorp-vault-auth-interface) is needed.
Each `HashicorpVaultAuth` implementation is packaged inside its own service extension. 
In this way, it can easily be added/removed from the runtime and maintained in one place.

Due to the possible differences in the needed configuration of different authentication methods, each Haschicorp Vault Auth Service Extension will need its own configuration values.
Since those configuration values are specific to the Hashicorp Vault Auth Service Extensions and have no influence on the Hashicorp Vault Extension, they will not be further discussed here.

### Hashicorp Vault Auth Implementation

Example Interface Implementation for `token-based` auth:

```java
public record HashicorpVaultTokenAuth(@NotNull String vaultToken) implements HashicorpVaultAuth {

    private String vaultToken;
    
    @Override
    public void login() {
        // no login is needed for the token authentication method since it is already given with the config
        // with other implementations like kubernetes auth, a `client_token` will be generated here
    }
    
}
```

### Hashicorp Vault Auth Registry

In line with the registry pattern, `HashicorpVaultAuthRegistry` and `HashicorpVaultAuthRegistryImpl` are created.
The `HashicorpVaultAuthRegistry` will be used to store one or more implementations of `HashicorpVaultAuth`, each representing a different authentication method.
More on the usage of the `HashicorpVaultAuthRegistry` in the [Hashicorp Vault Auth Registration](#hashicorp-vault-auth-registration) section.

```java
public interface HashicorpVaultAuthRegistry {
    
    void register(String method, HashicorpVaultAuth authImplementation);
    
    @NotNull
    HashicorpVaultAuth resolve(String method);
    
    boolean hasService(String method);
}
```

```java
public class HashicorpVaultAuthRegistryImpl implements HashicorpVaultAuthRegistry {
    
    private final Map<String, HashicorpVaultAuth> services = new HashMap<>();

    public HashicorpVaultAuthRegistryImpl() {
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

### Hashicorp Vault Auth Registration

During the `initialize()` call, the service extension providing an auth method will register an instance of its `HashicorpVaultAuth` implementation in the `HashicorpVaultAuthRegistry`.
The `HashicorpVaultAuthRegistry` is provided to the service extension through use of the provider pattern by the `HashicorpVaultExtension`.

```java
@Provider
public HashicorpVaultAuthRegistry hashicorpVaultAuthRegistry() {
    return new HashicorpVaultAuthRegistryImpl();
}
```

Inside the service extension providing an `HashicorpVaultAuth` implementation, the `HashicorpVaultAuthRegistry` is injected.

```java
@Inject
private HashicorpVaultAuthRegistry hashicorpVaultAuthRegistry;
```

The injected `HashicorpVaultAuthRegistry` is used to register the `HashicorpVaultAuth` implementation.

```java
@Override
public void initialize(ServiceExtensionContext context) {
    var token = context.getSetting(VAULT_TOKEN, null);

    if (!hashicorpVaultAuthRegistry.hasService("token-based")) {
        hashicorpVaultAuthRegistry.register("token-based", HashicorpVaultTokenAuth(token));
    }

}
```

### Configuration

A new config value is introduced to the HashiCorp Vault Extension named `edc.vault.hashicorp.auth.method`.
`edc.vault.hashicorp.auth.method` governs which `HashicorpVaultAuth` implementation is used from `HashicorpVaultAuthRegistry` and is persisted in `HashicorpVaultSettings`.

### HashiCorp Vault Client

Since the `HashicorpVaultClient` contains a lot of authentication logic, it will also go through a refactoring.
The goal of the refactoring, is the removal of the token authentication logic from `HashicorpVaultClient` and to make the authentication logic interchangeable.
`VaultAuthenticationRegistry` is passed to `HashicorpVaultClient` during creation by `HashicorpVaultServiceExtension`.
`HashicorpVaultClient` will use `VaultAuthenticationRegistry` based on `edc.vault.hashicorp.auth.method` setting to fetch the `client_token` that is provided by the chosen `HashicorpVaultAuth` implementation.
`client_token` will then be used to generate the Headers for the HTTP requests.

Old `getHeaders()`:

```java
@NotNull
private Headers getHeaders() {
    var headersBuilder = new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true));
    headersBuilder.add(VAULT_TOKEN_HEADER, settings.token());
    return headersBuilder.build();
}
```

New `getHeaders()`:

```java
@NotNull
private Headers getHeaders() {
    var headersBuilder = new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true));
    headersBuilder.add(VAULT_TOKEN_HEADER, vaultAuthenticationRegistry.resolve(settings.getAuthMethod()).vaultToken());
    return headersBuilder.build();
}
```

### HashiCorp Vault Token Auth Extension

The token based authentication logic is refactored and moved into its own extension named `HashiCorp Vault Token Auth`.
This includes the token refresh functionality and will lead to a refactoring of the following classes:

* `HashicorpVaultExtension.java`
* `HashicorpVaultClient.java`
* `HashicorpVaultSettings.java`
* `HashicorpVaultTokenRenewTask.java`


## Further Considerations

Currently, the authentication method is chosen through configuration and unable to be changed at runtime.
If the need arises in the future, the authentication method may be chosen through the request headers instead while keeping the configuration setting as default.
This option was explicitly left out in this decision record, to keep the refactored code as close to the original as possible in terms of functionality and API.