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
 *       Mercedes-Benz Tech Innovation GmbH - Initial API and Implementation
 *
 */

package org.eclipse.edc.vault.hashicorp.auth.kubernetes.client;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;

import static java.util.Objects.requireNonNull;

@Settings
public class HashicorpVaultKubernetesSettings {

    public static final String VAULT_URL = "edc.vault.hashicorp.url";
    public static final String VAULT_KUBERNETES_AUTH_ROLE = "edc.vault.hashicorp.auth.kubernetes.role";
    public static final String VAULT_SERVICE_ACCOUNT_TOKEN = "edc.vault.hashicorp.auth.kubernetes.service.account.token";
    public static final String VAULT_SERVICE_ACCOUNT_TOKEN_PATH = "edc.vault.hashicorp.auth.kubernetes.service.account.token.path";
    public static final String VAULT_KUBERNETES_EXPIRATION_THRESHOLD_SECONDS = "edc.vault.hashicorp.auth.kubernetes.expiration.threshold.seconds";

    public static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @Setting(description = "The URL of the Hashicorp Vault", key = VAULT_URL)
    private String vaultUrl;

    @Setting(description = "The role that should be requested while using the kubernetes authentication method", defaultValue = "", key = VAULT_KUBERNETES_AUTH_ROLE)
    private String vaultK8sAuthRole;

    @Setting(description = "The token associated with the Kubernetes service account, only use for demo/testing purposes", required = false, key = VAULT_SERVICE_ACCOUNT_TOKEN)
    private String serviceAccountToken;

    @Setting(description = "The expiration threshold for token renewal", required = false, defaultValue = "30", key = VAULT_KUBERNETES_EXPIRATION_THRESHOLD_SECONDS)
    private long expirationThresholdSeconds;

    @Setting(description = "The Path to the Kubernetes Service Account Token", required = false, defaultValue = DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH, key = VAULT_SERVICE_ACCOUNT_TOKEN_PATH)
    private String serviceAccountTokenPath;

    private HashicorpVaultKubernetesSettings() {

    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultK8sAuthRole() {
        return vaultK8sAuthRole;
    }

    public String getServiceAccountToken() {
        return serviceAccountToken;
    }

    public long getExpirationThresholdSeconds() {
        return expirationThresholdSeconds;
    }

    public String getServiceAccountTokenPath() {return serviceAccountTokenPath;}

    public static class Builder {

        private final HashicorpVaultKubernetesSettings config;

        private Builder() {
            config = new HashicorpVaultKubernetesSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder vaultUrl(String url) {
            requireNonNull(url, "Vault url must not be null");
            //this.config.vaultUrl = HttpUrl.parse(url);
            this.config.vaultUrl = url;
            return this;
        }

        public Builder vaultK8sAuthRole(String vaultK8sAuthRole) {
            requireNonNull(vaultK8sAuthRole, "Kubernetes Auth Role must not be null");
            this.config.vaultK8sAuthRole = vaultK8sAuthRole;
            return this;
        }

        public Builder serviceAccountToken(String vaultToken) {
            this.config.serviceAccountToken = vaultToken;
            return this;
        }

        public Builder serviceAccountTokenPath(String serviceAccountTokenPath) {
            this.config.serviceAccountTokenPath = serviceAccountTokenPath;
            return this;
        }

        public Builder expirationThresholdSeconds(long expirationThresholdSeconds) {
            this.config.expirationThresholdSeconds = Math.max(0, expirationThresholdSeconds);
            return this;
        }

        public HashicorpVaultKubernetesSettings build() {
            return config;
        }
    }
}
