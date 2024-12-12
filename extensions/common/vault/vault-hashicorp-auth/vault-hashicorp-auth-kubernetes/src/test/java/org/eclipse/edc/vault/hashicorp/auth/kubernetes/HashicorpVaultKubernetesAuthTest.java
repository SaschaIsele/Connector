/*
 * Copyright (c) 2023 ZF Friedrichshafen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesLoginResponsePayload;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesSettings;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.util.FileUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesSettings.DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH;
import static org.mockito.ArgumentMatchers.any;

class HashicorpVaultKubernetesAuthTest {

    private final String vaultRole = "role";

    private final String vaultToken = "token";

    HashicorpVaultKubernetesAuthClient authClient;

    FileUtil fileUtil;

    Clock clock;

    HashicorpVaultKubernetesSettings settings;

    @BeforeEach
    void setup() throws IOException {
        authClient = Mockito.mock(HashicorpVaultKubernetesAuthClient.class);
        fileUtil = Mockito.mock(FileUtil.class);

        long fixedClockTime = 1672574400; // 1-1-2023 13:00:00
        clock = Clock.fixed(Instant.ofEpochSecond(fixedClockTime), ZoneId.of("Europe/Berlin"));

        Long expirationDurationSeconds = 60L;
        var auth = new HashicorpVaultKubernetesLoginResponsePayload.Auth(vaultToken, expirationDurationSeconds);
        var successPayload = new HashicorpVaultKubernetesLoginResponsePayload(auth);

        Mockito.when(authClient.loginWithKubernetesAuth(any(), any()))
            .thenReturn(Result.success(successPayload));
        Mockito.when(fileUtil.readContentsFromFile(DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH))
            .thenReturn("my-service-token-jwt");
    }

    @Test
    void throwsExceptionWhenLoginFails() {
        Mockito.when(authClient.loginWithKubernetesAuth(any(), any()))
            .thenReturn(Result.failure("Failed"));

        HashicorpVaultKubernetesAuthImpl auth = new HashicorpVaultKubernetesAuthImpl(vaultRole, authClient, clock, fileUtil);

        Assertions.assertThrows(HashicorpVaultException.class, auth::login);
    }

    @Test
    void setsTokenWhenLoginSucceeds() {
        HashicorpVaultKubernetesAuthImpl auth = new HashicorpVaultKubernetesAuthImpl(vaultRole, authClient, clock, fileUtil);
        auth.login();

        Assertions.assertEquals(auth.vaultToken(), vaultToken);
    }

    @Test
    void setsCorrectTokenExpirationTime() {
        LocalDateTime expectedExpirationTime = LocalDateTime.of(2023, 1, 1, 13, 1, 0);

        HashicorpVaultKubernetesAuthImpl auth = new HashicorpVaultKubernetesAuthImpl(vaultRole, authClient, fileUtil, clock);
        auth.login();

        Assertions.assertEquals(auth.getTokenExpirationTimestamp(), expectedExpirationTime);
    }

    @Test
    void returnsFalseWhenTokenShouldNotGetRenewed() {
        HashicorpVaultKubernetesAuthImpl auth = new HashicorpVaultKubernetesAuthImpl(vaultRole, authClient, fileUtil, clock);
        auth.login();

        // Expiration in 1 min so we don't expect a renewal
        Assertions.assertFalse(auth.shouldTokenBeRenewed());
    }

    @Test
    void returnsTrueWhenTokenShouldGetRenewed() {
        var auth = new HashicorpVaultKubernetesLoginResponsePayload.Auth(vaultToken, 10L);
        var successPayload = new HashicorpVaultKubernetesLoginResponsePayload(auth);

        Mockito.when(authClient.loginWithKubernetesAuth(any(), any()))
            .thenReturn(Result.success(successPayload));

        HashicorpVaultKubernetesAuthImpl kubernetesAuth = new HashicorpVaultKubernetesAuthImpl(vaultRole, authClient, fileUtil, clock);
        kubernetesAuth.login();

        // Expiration in 10 sec so we expect a renewal
        Assertions.assertTrue(kubernetesAuth.shouldTokenBeRenewed());
    }
}
