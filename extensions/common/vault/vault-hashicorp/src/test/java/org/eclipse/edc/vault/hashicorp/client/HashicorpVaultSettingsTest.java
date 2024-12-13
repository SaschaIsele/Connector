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

package org.eclipse.edc.vault.hashicorp.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashicorpVaultSettingsTest {

    private static final String TOKEN = "token";
    private static final String URL = "https://test.com/vault";
    private static final String HEALTH_CHECK_PATH = "/healthcheck/path";
    private static final String SECRET_PATH = "/secret/path";

    @Test
    void createSettings_withDefaultValues_shouldSucceed() {
        var settings = assertDoesNotThrow(() -> createSettings(
                URL,
                TOKEN,
                HEALTH_CHECK_PATH));
        assertThat(settings.url()).isEqualTo(URL);
        assertThat(settings.healthCheckEnabled()).isEqualTo(true);
        assertThat(settings.healthCheckPath()).isEqualTo(HEALTH_CHECK_PATH);
        assertThat(settings.healthStandbyOk()).isEqualTo(true);
        //assertThat(settings.token()).isEqualTo(TOKEN);
        assertThat(settings.secretPath()).isEqualTo(SECRET_PATH);
        assertThat(settings.getFolderPath()).isNull();
    }

    @Test
    void createSettings_withVaultUrlNull_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                null,
                TOKEN,
                HEALTH_CHECK_PATH));
        assertThat(throwable.getMessage()).isEqualTo("Vault url must not be null");
    }

    @Test
    void createSettings_withHealthCheckPathNull_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                URL,
                TOKEN,
                null));
        assertThat(throwable.getMessage()).isEqualTo("Vault health check path must not be null");
    }

    @Test
    void createSettings_withVaultTokenNull_shouldThrowException() {
        var throwable = assertThrows(Exception.class, () -> createSettings(
                URL,
                null,
                HEALTH_CHECK_PATH));
        assertThat(throwable.getMessage()).isEqualTo("Vault token must not be null");
    }

    private HashicorpVaultSettings createSettings(String url,
                                                  String token,
                                                  String healthCheckPath) {
        return HashicorpVaultSettings.Builder.newInstance()
                .url(url)
                .healthCheckEnabled(true)
                .healthCheckPath(healthCheckPath)
                .healthStandbyOk(true)
                //.token(token)
                .secretPath(SECRET_PATH)
                .build();
    }
}
