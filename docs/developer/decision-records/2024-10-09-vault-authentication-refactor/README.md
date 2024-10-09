# Decision Record Refactor of the HashiCorp Vault extension

## Decision

Refactor the HashiCorp Vault extension and make it extensible with different methods of authentication.

## Rationale

The current implementation of the authentication in the HashiCorp Vault extension has no way to add new authentication methods and to switch between them.
It goes against the EDC extensibility model and needs to be remedied.
Additionally, extracting the current implementation of the token authentication into its own extension will improve readability and maintainability of the code.

## Affected Areas

HashiCorp Vault extension

## Approach

To allow an extensible authentication module for HashiCorp Vault, an implementation similar to the existing management API authentication is possible.

### Vault Authentication Service Interface

To interact with a multitude of different authentication methods, an interface for the Vault Authentication Service is needed.
This interface will contain two methods:

* `login()`
* `getVaultToken()`

`login()` contains the authentication and fetches/generates the `client_token` from HashiCorp Vault.

`getVaultToken()` returns the saved `client_token` when needed.S

### Vault Authentication Service Extension

The Vault Authentication Service Extensions each implement a single HashiCorp Vault Authentication method.
To achieve this, each extension contains an implementation of the newly introduced Vault Authentication Service interface.
The goal of each extension is to authenticate with HashiCorp Vault and to provide a valid `client_token` to the `HashicorpVaultClient`.

### Registration

Each Service Extension will register itself on startup in the `VaultAuthenticationRegistry`.
This is done through an `@Provider` method inside the HashiCorp Vault extension.

```java
    @Provider
    public VaultAuthenticationRegistry vaultAuthenticationRegistry() {
        return new VaultAuthenticationRegistryImpl();
    }
```
S
Afterwards, the `VaultAuthenticationRegistry` is given injected into the Vault Authentication Service Extensions through `@Inject`.

```java
    @Inject
    private VaultAuthenticationRegistry vaultAuthenticationRegistry;
```

### Configuration

A new config value is introduced to the HashiCorp Vault Extension named `edc.vault.hashicorp.auth.method`. This value governs which Vault Authentication Service Extension is used from the Vault Auth Registry.

Due to the possible differences in the configuration of each authentication method, each Vault Authentication Service Extension comes with its own configuration values.

### HashiCorp Vault Client

Since the client contains a lot of authentication logic, it will also go through a refactoring.
After the refactoring it will only use the `VaultAuthenticationRegistry` based on the configured `edc.vault.hashicorp.auth.method` to fetch the `client_token` that is provided by the Vault Authentication Service Extensions.

### HashiCorp Vault Token Authentication Extension

The current token based authentication is refactored and moved into its own extension named `hashicorp-auth-tokenbased`.
This also includes the token refresh functionality.
