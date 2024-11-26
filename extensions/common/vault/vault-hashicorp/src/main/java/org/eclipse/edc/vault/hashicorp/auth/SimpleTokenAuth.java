package org.eclipse.edc.vault.hashicorp.auth;

import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuth;

public class SimpleTokenAuth implements HashicorpVaultAuth {
    private String vaultToken;

    SimpleTokenAuth(String vaultToken) {
        this.vaultToken = vaultToken;
    }

    @Override
    public String vaultToken() {
        return vaultToken;
    }
}
