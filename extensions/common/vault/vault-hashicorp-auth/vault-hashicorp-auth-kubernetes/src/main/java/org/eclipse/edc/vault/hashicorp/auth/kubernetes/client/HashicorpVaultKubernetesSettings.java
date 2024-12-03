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
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.time.Duration;
import java.util.Objects;

import static java.lang.String.format;

@Settings
public class HashicorpVaultKubernetesSettings {

    public static final String VAULT_URL = "edc.vault.hashicorp.url";
    public static final String VAULT_KUBERNETES_AUTH_ROLE = "edc.vault.hashicorp.auth.kubernetes.role";
    public static final String VAULT_TIMEOUT_SECONDS = "edc.vault.hashicorp.timeout.seconds";
    public static final String VAULT_TOKEN = "edc.vault.hashicorp.token";
    public static final String VAULT_TOKEN_RENEW = "edc.vault.hashicorp.token.renew";
    public static final String VAULT_SERVICE_ACCOUNT_TOKEN_PATH = "edc.vault.hashicorp.token.service-account-token-path";
    public static final int VAULT_TIMEOUT_SECONDS_DEFAULT = 30;
    public static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @Setting(description = "Sets the timeout for HTTP requests to the vault, in seconds", required = false, defaultValue = "30", type = "integer", key = VAULT_TIMEOUT_SECONDS )
    private Duration timeout;

    @Setting(description = "The URL of the Hashicorp Vault", key = VAULT_URL)
    private String vaultUrl;

    @Setting(description = "The role that should be requested while using the kubernetes authentication method", defaultValue = "", key = VAULT_KUBERNETES_AUTH_ROLE)
    private String vaultK8sAuthRole;

    @Setting(description = "The token used to access the Hashicorp Vault", key = VAULT_TOKEN)
    private String vaultToken;

    @Setting(description = "", defaultValue = "true", key = VAULT_TOKEN_RENEW)
    private Boolean renewToken;

    @Setting(description = "The Path to the Kubernetes Service Account Token", required = false, defaultValue = DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH, key = VAULT_SERVICE_ACCOUNT_TOKEN_PATH)
    private String serviceAccountTokenPath;

    private HashicorpVaultKubernetesSettings() {

    }

    public static HashicorpVaultKubernetesSettings create(ServiceExtensionContext context) {
        var vaultUrl = context.getSetting(VAULT_URL, null);
        if (vaultUrl == null) {
            throw new EdcException(format("Vault URL (%s) must be defined", VAULT_URL));
        }

        var vaultTimeoutSeconds = Math.max(0, context.getSetting(VAULT_TIMEOUT_SECONDS, VAULT_TIMEOUT_SECONDS_DEFAULT));
        var vaultTimeoutDuration = Duration.ofSeconds(vaultTimeoutSeconds);

        var vaultToken = context.getSetting(VAULT_TOKEN, null);

        if (vaultToken == null) {
            throw new EdcException(format("For Vault authentication [%s] is required", VAULT_TOKEN));
        }

        var vaultK8sAuthRole = context.getSetting(VAULT_KUBERNETES_AUTH_ROLE, null);

        var renewToken = context.getSetting(VAULT_TOKEN_RENEW, true);

        var serviceAccountTokenPath = context.getSetting(VAULT_SERVICE_ACCOUNT_TOKEN_PATH, DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH);

        return Builder.newInstance()
                .vaultUrl(vaultUrl)
                .vaultK8sAuthRole(vaultK8sAuthRole)
                .vaultToken(vaultToken)
                .timeout(vaultTimeoutDuration)
                .build();
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultK8sAuthRole() {
        return vaultK8sAuthRole;
    }

    public String vaultToken() {
        return vaultToken;
    }

    public Duration timeout() {
        return timeout;
    }

    public Boolean renewToken() {return renewToken;}

    public String getServiceAccountTokenPath() {return serviceAccountTokenPath;}

    public static class Builder {

        private final HashicorpVaultKubernetesSettings config;

        private Builder() {
            config = new HashicorpVaultKubernetesSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder vaultUrl(String vaultUrl) {
            this.config.vaultUrl = vaultUrl;
            return this;
        }

        public Builder vaultK8sAuthRole(String vaultK8sAuthRole) {
            this.config.vaultK8sAuthRole = vaultK8sAuthRole;
            return this;
        }

        public Builder vaultToken(String vaultToken) {
            this.config.vaultToken = vaultToken;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.config.timeout = timeout;
            return this;
        }

        public Builder renewToken(Boolean renewToken) {
            this.config.renewToken = renewToken;
            return this;
        }

        public Builder serviceAccountTokenPath(String serviceAccountTokenPath) {
            this.config.serviceAccountTokenPath = serviceAccountTokenPath;
            return this;
        }

        public HashicorpVaultKubernetesSettings build() {
            return config;
        }
    }
}
