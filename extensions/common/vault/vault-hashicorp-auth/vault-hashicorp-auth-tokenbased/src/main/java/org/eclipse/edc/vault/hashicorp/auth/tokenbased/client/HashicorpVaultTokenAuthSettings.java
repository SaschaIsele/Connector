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
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;

import static java.util.Objects.requireNonNull;

/**
 * Settings for the {@link HashicorpVaultTokenAuthClient}.
 */
@Settings
public class HashicorpVaultTokenAuthSettings {

    public static final String VAULT_URL = "edc.vault.hashicorp.url";
    public static final String VAULT_TOKEN = "edc.vault.hashicorp.token";
    public static final String VAULT_TOKEN_SCHEDULED_RENEW_ENABLED = "edc.vault.hashicorp.token.scheduled-renew-enabled";
    public static final String VAULT_TOKEN_TTL = "edc.vault.hashicorp.token.ttl";
    public static final String VAULT_TOKEN_RENEW_BUFFER = "edc.vault.hashicorp.token.renew-buffer";
    public static final boolean VAULT_TOKEN_SCHEDULED_RENEW_ENABLED_DEFAULT = true;
    public static final long VAULT_TOKEN_RENEW_BUFFER_DEFAULT = 30;
    public static final long VAULT_TOKEN_TTL_DEFAULT = 300;

    @Setting(description = "The URL of the Hashicorp Vault", required = true, key = VAULT_URL)
    private HttpUrl url;

    @Setting(description = "The token used to access the Hashicorp Vault", required = true, key = VAULT_TOKEN)
    private String token;

    @Setting(description = "Whether the automatic token renewal process will be triggered or not. Should be disabled only for development and testing purposes", defaultValue = "true", key = VAULT_TOKEN_SCHEDULED_RENEW_ENABLED)
    private boolean scheduledTokenRenewEnabled;

    @Setting(description = "The time-to-live (ttl) value of the Hashicorp Vault token in seconds", defaultValue = "300", type = "long", key = VAULT_TOKEN_TTL)
    private long ttl;

    @Setting(description = "The renew buffer of the Hashicorp Vault token in seconds", defaultValue = "30", type = "long", key = VAULT_TOKEN_RENEW_BUFFER)
    private long renewBuffer;

    private HashicorpVaultTokenAuthSettings() {
    }

    public HttpUrl url() {
        return url;
    }

    public String token() {
        return token;
    }

    public boolean scheduledTokenRenewEnabled() {
        return scheduledTokenRenewEnabled;
    }

    public long ttl() {
        return ttl;
    }

    public long renewBuffer() {
        return renewBuffer;
    }

    public static class Builder {
        private final HashicorpVaultTokenAuthSettings values;

        private Builder() {
            values = new HashicorpVaultTokenAuthSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder url(String url) {
            requireNonNull(url, "Vault url must not be null");
            values.url = HttpUrl.parse(url);
            return this;
        }

        public Builder token(String token) {
            values.token = token;
            return this;
        }

        public Builder scheduledTokenRenewEnabled(boolean scheduledTokenRenewEnabled) {
            values.scheduledTokenRenewEnabled = scheduledTokenRenewEnabled;
            return this;
        }

        public Builder ttl(long ttl) {
            values.ttl = ttl;
            return this;
        }

        public Builder renewBuffer(long renewBuffer) {
            values.renewBuffer = renewBuffer;
            return this;
        }

        public HashicorpVaultTokenAuthSettings build() {
            requireNonNull(values.url, "Vault url must be valid");
            requireNonNull(values.token, "Vault token must not be null");

            if (values.ttl < 5) {
                throw new IllegalArgumentException("Vault token ttl minimum value is 5");
            }

            if (values.renewBuffer >= values.ttl) {
                throw new IllegalArgumentException("Vault token renew buffer value must be less than ttl value");
            }

            return values;
        }
    }
}
