/*
 *  Copyright (c) 2024 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Implement automatic Hashicorp Vault token renewal
 *       Cofinity-X GmbH - Authentication refactoring
 *
 */

package org.eclipse.edc.vault.hashicorp.auth.tokenbased.client;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.vault.hashicorp.auth.tokenbased.client.HashicorpVaultTokenAuthSettings.VAULT_TOKEN_RENEW_BUFFER_DEFAULT;
import static org.eclipse.edc.vault.hashicorp.auth.tokenbased.client.HashicorpVaultTokenAuthSettings.VAULT_TOKEN_TTL_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashicorpVaultTokenAuthSettingsTest {

    private static final String TOKEN = "token";
    private static final String URL = "https://test.com/vault";
    private static final HttpUrl HTTP_URL = HttpUrl.parse(URL);

    @Test
    void createSettings_withDefaultValues_shouldSucceed() {
        var settings = assertDoesNotThrow(() -> createSettings(
                URL,
                TOKEN,
                VAULT_TOKEN_TTL_DEFAULT,
                VAULT_TOKEN_RENEW_BUFFER_DEFAULT));
        assertThat(settings.url()).isEqualTo(HTTP_URL);
        assertThat(settings.token()).isEqualTo(TOKEN);
        assertThat(settings.ttl()).isEqualTo(VAULT_TOKEN_TTL_DEFAULT);
        assertThat(settings.renewBuffer()).isEqualTo(VAULT_TOKEN_RENEW_BUFFER_DEFAULT);
    }

    @Test
    void createSettings_withVaultUrlNull_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                null,
                TOKEN,
                VAULT_TOKEN_TTL_DEFAULT,
                VAULT_TOKEN_RENEW_BUFFER_DEFAULT));
        assertThat(throwable.getMessage()).isEqualTo("Vault url must not be null");
    }

    @Test
    void createSettings_withVaultUrlInvalid_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                "this is not valid",
                TOKEN,
                VAULT_TOKEN_TTL_DEFAULT,
                VAULT_TOKEN_RENEW_BUFFER_DEFAULT));
        assertThat(throwable.getMessage()).isEqualTo("Vault url must be valid");
    }

    @Test
    void createSettings_withVaultTokenNull_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                URL,
                null,
                VAULT_TOKEN_TTL_DEFAULT,
                VAULT_TOKEN_RENEW_BUFFER_DEFAULT));
        assertThat(throwable.getMessage()).isEqualTo("Vault token must not be null");
    }

    @Test
    void createSettings_withVaultTokenTtlLessThan5_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                URL,
                TOKEN,
                4,
                VAULT_TOKEN_RENEW_BUFFER_DEFAULT));
        assertThat(throwable.getMessage()).isEqualTo("Vault token ttl minimum value is 5");
    }

    @ParameterizedTest
    @ValueSource(longs = { VAULT_TOKEN_TTL_DEFAULT, VAULT_TOKEN_TTL_DEFAULT + 1 })
    void createSettings_withVaultTokenRenewBufferEqualOrGreaterThanTtl_shouldThrowException(long value) {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                URL,
                TOKEN,
                VAULT_TOKEN_TTL_DEFAULT,
                value));
        assertThat(throwable.getMessage()).isEqualTo("Vault token renew buffer value must be less than ttl value");
    }

    private HashicorpVaultTokenAuthSettings createSettings(String url,
                                                           String token,
                                                           long ttl,
                                                           long renewBuffer) {
        return HashicorpVaultTokenAuthSettings.Builder.newInstance()
                .url(url)
                .token(token)
                .ttl(ttl)
                .renewBuffer(renewBuffer)
                .build();
    }
}
