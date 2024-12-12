/*
 *  Copyright (c) 2022 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial Test
 *       Bayerische Motoren Werke Aktiengesellschaft - Refactoring
 *
 */

package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import org.eclipse.edc.junit.annotations.ComponentTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.spi.security.Vault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.vault.hashicorp.client.HashicorpVaultSettings.VAULT_FALLBACK_TOKEN_KEY;
import static org.eclipse.edc.vault.hashicorp.client.HashicorpVaultSettings.VAULT_URL_KEY;

@ComponentTest
@Testcontainers
class HashicorpVaultKubernetesAuthIntegrationTest {
    static final String DOCKER_IMAGE_NAME = "vault:1.9.6";
    static final String VAULT_ENTRY_KEY = "testing";
    static final String VAULT_ENTRY_VALUE = UUID.randomUUID().toString();
    static final String VAULT_DATA_ENTRY_NAME = "content";
    static final String TOKEN = UUID.randomUUID().toString();

    @Container
    private static final VaultContainer<?> VAULTCONTAINER = new VaultContainer<>(DOCKER_IMAGE_NAME)
            .withVaultToken(TOKEN)
            .withSecretInVault("secret/" + VAULT_ENTRY_KEY, format("%s=%s", VAULT_DATA_ENTRY_NAME, VAULT_ENTRY_VALUE));


    @RegisterExtension
    protected static RuntimeExtension runtime = new RuntimePerClassExtension(new EmbeddedRuntime("vault-runtime",
            getConfig(),
            "extensions:common:vault:vault-hashicorp",
            "extensions:common:vault:vault-hashicorp-auth:vault-hashicorp-auth-kubernetes"));


    @Test
    @DisplayName("Resolve a secret that exists")
    void testResolveSecret_exists(Vault vault) {
        var secretValue = vault.resolveSecret(VAULT_ENTRY_KEY);
        assertThat(secretValue).isEqualTo(VAULT_ENTRY_VALUE);
    }


    private static Map<String, String> getConfig() {
        // container might not be started, lazily start and wait for it to come up
        if (!VAULTCONTAINER.isRunning()) {
            VAULTCONTAINER.start();
            VAULTCONTAINER.waitingFor(Wait.forHealthcheck());
        }
        return new HashMap<>() {
            {
                put(VAULT_URL_KEY, "http://%s:%d".formatted(VAULTCONTAINER.getHost(), VAULTCONTAINER.getFirstMappedPort()));
                put(VAULT_FALLBACK_TOKEN_KEY, TOKEN);
            }
        };
    }
}
