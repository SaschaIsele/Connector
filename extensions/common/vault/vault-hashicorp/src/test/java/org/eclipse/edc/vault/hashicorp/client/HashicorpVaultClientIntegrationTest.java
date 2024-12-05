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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import org.eclipse.edc.junit.annotations.ComponentTest;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.vault.hashicorp.auth.HashicorpVaultAuthRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.UUID;

import static org.eclipse.edc.http.client.testfixtures.HttpTestUtils.testHttpClient;


class HashicorpVaultClientIntegrationTest {

    @ComponentTest
    @Testcontainers
    @Nested
    abstract static class Tests {

        protected static final String HTTP_URL_FORMAT = "http://%s:%s";
        protected static final String HEALTH_CHECK_PATH = "/health/path";
        protected static final String CLIENT_TOKEN_KEY = "client_token";
        protected static final String AUTH_KEY = "auth";
        protected static final String TOKEN = UUID.randomUUID().toString();
        protected HashicorpVaultClient client;
        protected final ObjectMapper mapper = new ObjectMapper();
        protected final ConsoleMonitor monitor = new ConsoleMonitor();
        protected final HashicorpVaultAuthRegistryImpl registry = new HashicorpVaultAuthRegistryImpl(TOKEN);

    }

    @ComponentTest
    @Testcontainers
    @Nested
    class LastKnownFoss extends Tests {
        @Container
        static final VaultContainer<?> VAULT_CONTAINER = new VaultContainer<>("vault:1.9.6")
                .withVaultToken(TOKEN);

        public static HashicorpVaultSettings getSettings() throws IOException, InterruptedException {
            var execResult = VAULT_CONTAINER.execInContainer(
                    "vault",
                    "token",
                    "create",
                    "-policy=root",
                    "-format=json");

            var jsonParser = Json.createParser(new StringReader(execResult.getStdout()));
            jsonParser.next();
            var auth = jsonParser.getObjectStream().filter(e -> e.getKey().equals(AUTH_KEY))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow()
                    .asJsonObject();
            var clientToken = auth.getString(CLIENT_TOKEN_KEY);

            return HashicorpVaultSettings.Builder.newInstance()
                    .url(HTTP_URL_FORMAT.formatted(VAULT_CONTAINER.getHost(), VAULT_CONTAINER.getFirstMappedPort()))
                    .healthCheckPath(HEALTH_CHECK_PATH)
                    .fallbackToken(clientToken)
                    .build();
        }

        @BeforeEach
        void beforeEach() throws IOException, InterruptedException {
            client = new HashicorpVaultClient(
                    testHttpClient(),
                    mapper,
                    monitor,
                    registry,
                    getSettings()
            );
        }
    }

    @ComponentTest
    @Testcontainers
    @Nested
    class Latest extends Tests {
        @Container
        static final VaultContainer<?> VAULT_CONTAINER = new VaultContainer<>("hashicorp/vault:1.17.3")
                .withVaultToken(UUID.randomUUID().toString());

        public static HashicorpVaultSettings getSettings() throws IOException, InterruptedException {
            var execResult = VAULT_CONTAINER.execInContainer(
                    "vault",
                    "token",
                    "create",
                    "-policy=root",
                    "-format=json");

            var jsonParser = Json.createParser(new StringReader(execResult.getStdout()));
            jsonParser.next();
            var auth = jsonParser.getObjectStream().filter(e -> e.getKey().equals(AUTH_KEY))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow()
                    .asJsonObject();
            var clientToken = auth.getString(CLIENT_TOKEN_KEY);

            return HashicorpVaultSettings.Builder.newInstance()
                    .url(HTTP_URL_FORMAT.formatted(VAULT_CONTAINER.getHost(), VAULT_CONTAINER.getFirstMappedPort()))
                    .healthCheckPath(HEALTH_CHECK_PATH)
                    .fallbackToken(clientToken)
                    .build();
        }

        @BeforeEach
        void beforeEach() throws IOException, InterruptedException {
            client = new HashicorpVaultClient(
                    testHttpClient(),
                    mapper,
                    monitor,
                    registry,
                    getSettings()
            );
        }
    }
}