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

import static java.util.Objects.requireNonNull;

/**
 * Settings for the {@link HashicorpVaultTokenAuthClient}.
 */
public class HashicorpVaultTokenAuthSettings {

    private HttpUrl url;
    private String token;
    private boolean scheduledTokenRenewEnabled;
    private long ttl;
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
