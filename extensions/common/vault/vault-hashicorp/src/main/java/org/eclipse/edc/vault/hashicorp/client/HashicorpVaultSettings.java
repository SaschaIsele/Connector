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

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;

import static java.util.Objects.requireNonNull;

/**
 * Settings for the {@link HashicorpVaultClient}.
 */
@Settings
public class HashicorpVaultSettings {
    public static final String VAULT_API_HEALTH_PATH_DEFAULT = "/v1/sys/health";
    public static final String VAULT_API_SECRET_PATH_DEFAULT = "/v1/secret";
    public static final boolean VAULT_HEALTH_CHECK_STANDBY_OK_DEFAULT = false;
    public static final boolean VAULT_HEALTH_CHECK_ENABLED_DEFAULT = true;
    public static final String VAULT_AUTH_DEFAULT = "fallbackToken";
    public static final String VAULT_AUTH_METHOD_KEY = "edc.vault.hashicorp.auth.method";
    public static final String VAULT_FALLBACK_TOKEN_KEY = "edc.vault.hashicorp.fallbackToken";
    public static final String VAULT_URL_KEY = "edc.vault.hashicorp.url";

    @Setting(description = "The URL of the Hashicorp Vault", key = VAULT_URL_KEY)
    private String url;
    @Setting(description = "Whether or not the vault health check is enabled", defaultValue = VAULT_HEALTH_CHECK_ENABLED_DEFAULT + "", key = "edc.vault.hashicorp.health.check.enabled")
    private boolean healthCheckEnabled;
    @Setting(description = "The URL path of the vault's /health endpoint", defaultValue = VAULT_API_HEALTH_PATH_DEFAULT, key = "edc.vault.hashicorp.api.health.check.path")
    private String healthCheckPath;
    @Setting(description = "Specifies if being a standby should still return the active status code instead of the standby status code", defaultValue = VAULT_HEALTH_CHECK_STANDBY_OK_DEFAULT + "", key = "edc.vault.hashicorp.health.check.standby.ok")
    private boolean healthStandbyOk;
    @Setting(description = "The URL path of the vault's /secret endpoint", defaultValue = VAULT_API_SECRET_PATH_DEFAULT, key = "edc.vault.hashicorp.api.secret.path")
    private String secretPath;
    @Setting(description = "The path of the folder that the secret is stored in, relative to VAULT_FOLDER_PATH", required = false, key = "edc.vault.hashicorp.folder")
    private String folderPath;
    @Setting(description = "The value that governs which auth method is used, defaults to the fallbackToken", required = false, defaultValue = VAULT_AUTH_DEFAULT, key = VAULT_AUTH_METHOD_KEY)
    private String authMethod;
    @Setting(description = "Token used for fallback when no authentication extension is registered, should only be used for testing/demo purposes", required = false, key = VAULT_FALLBACK_TOKEN_KEY)
    private String fallbackToken;

    private HashicorpVaultSettings() {
    }

    public String url() {
        return url;
    }

    public boolean healthCheckEnabled() {
        return healthCheckEnabled;
    }

    public String healthCheckPath() {
        return healthCheckPath;
    }

    public boolean healthStandbyOk() {
        return healthStandbyOk;
    }

    public String secretPath() {
        return secretPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFallbackToken() {
        return fallbackToken;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public static class Builder {
        private final HashicorpVaultSettings values;

        private Builder() {
            values = new HashicorpVaultSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder url(String url) {
            requireNonNull(url, "Vault url must not be null");
            values.url = url;
            return this;
        }

        public Builder healthCheckEnabled(boolean healthCheckEnabled) {
            values.healthCheckEnabled = healthCheckEnabled;
            return this;
        }

        public Builder healthCheckPath(String healthCheckPath) {
            values.healthCheckPath = healthCheckPath;
            return this;
        }

        public Builder healthStandbyOk(boolean healthStandbyOk) {
            values.healthStandbyOk = healthStandbyOk;
            return this;
        }

        public Builder secretPath(String secretPath) {
            values.secretPath = secretPath;
            return this;
        }

        public Builder folderPath(String folderPath) {
            values.folderPath = folderPath;
            return this;
        }

        public Builder authMethod(String authMethod) {
            values.authMethod = authMethod;
            return this;
        }

        public Builder fallbackToken(String fallbackToken) {
            values.fallbackToken = fallbackToken;
            return this;
        }

        public HashicorpVaultSettings build() {
            requireNonNull(values.url, "Vault url must be valid");
            requireNonNull(values.healthCheckPath, "Vault health check path must not be null");

            return values;
        }
    }
}
