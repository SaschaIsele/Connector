/*
 *  Copyright (c) 2024 Cofinity-X GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Cofinity-X GmbH - Initial API and Implementation
 *
 */

package org.eclipse.edc.vault.hashicorp.auth.tokenbased;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuthRegistry;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.vault.hashicorp.auth.tokenbased.client.HashicorpVaultTokenAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.tokenbased.client.HashicorpVaultTokenAuthSettings;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Extension(value = HashicorpVaultTokenAuthExtension.NAME)
public class HashicorpVaultTokenAuthExtension implements ServiceExtension {
    public static final String NAME = "Hashicorp Vault Auth Tokenbased";

    public static final boolean VAULT_TOKEN_SCHEDULED_RENEW_ENABLED_DEFAULT = true;
    public static final long VAULT_TOKEN_RENEW_BUFFER_DEFAULT = 30;
    public static final long VAULT_TOKEN_TTL_DEFAULT = 300;

    @Setting(value = "The URL of the Hashicorp Vault", required = true)
    public static final String VAULT_URL = "edc.vault.hashicorp.url";

    @Setting(value = "The token used to access the Hashicorp Vault", required = true)
    public static final String VAULT_TOKEN = "edc.vault.hashicorp.token";

    @Setting(value = "Whether the automatic token renewal process will be triggered or not. Should be disabled only for development and testing purposes", defaultValue = "true")
    public static final String VAULT_TOKEN_SCHEDULED_RENEW_ENABLED = "edc.vault.hashicorp.token.scheduled-renew-enabled";

    @Setting(value = "The time-to-live (ttl) value of the Hashicorp Vault token in seconds", defaultValue = "300", type = "long")
    public static final String VAULT_TOKEN_TTL = "edc.vault.hashicorp.token.ttl";

    @Setting(value = "The renew buffer of the Hashicorp Vault token in seconds", defaultValue = "30", type = "long")
    public static final String VAULT_TOKEN_RENEW_BUFFER = "edc.vault.hashicorp.token.renew-buffer";

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    @Inject
    private HashicorpVaultAuthRegistry registry;

    private HashicorpVaultTokenAuthClient client;
    private HashicorpVaultTokenRenewTask tokenRenewalTask;
    private Monitor monitor;
    private HashicorpVaultTokenAuthSettings settings;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        settings = getSettings(context);
        registry.register("token-based", new HashicorpVaultAuthTokenImpl(settings.token()));
        tokenRenewalTask = new HashicorpVaultTokenRenewTask(
                NAME,
                executorInstrumentation,
                hashicorpVaultTokenAuthClient(),
                settings.renewBuffer(),
                monitor);
    }

    @Override
    public void start() {
        if (settings.scheduledTokenRenewEnabled()) {
            tokenRenewalTask.start();
        }
    }

    @Override
    public void shutdown() {
        if (tokenRenewalTask.isRunning()) {
            tokenRenewalTask.stop();
        }
    }

    private HashicorpVaultTokenAuthSettings getSettings(ServiceExtensionContext context) {
        var url = context.getSetting(VAULT_URL, null);
        var token = context.getSetting(VAULT_TOKEN, null);
        var isScheduledTokenRenewEnabled = context.getSetting(VAULT_TOKEN_SCHEDULED_RENEW_ENABLED, VAULT_TOKEN_SCHEDULED_RENEW_ENABLED_DEFAULT);
        var ttl = context.getSetting(VAULT_TOKEN_TTL, VAULT_TOKEN_TTL_DEFAULT);
        var renewBuffer = context.getSetting(VAULT_TOKEN_RENEW_BUFFER, VAULT_TOKEN_RENEW_BUFFER_DEFAULT);

        return HashicorpVaultTokenAuthSettings.Builder.newInstance()
                .url(url)
                .token(token)
                .scheduledTokenRenewEnabled(isScheduledTokenRenewEnabled)
                .ttl(ttl)
                .renewBuffer(renewBuffer)
                .build();
    }

    private HashicorpVaultTokenAuthClient hashicorpVaultTokenAuthClient() {
        if (client == null) {
            // the default type manager cannot be used as the Vault is a primordial service loaded at boot
            var mapper = new ObjectMapper();
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

            client = new HashicorpVaultTokenAuthClient(
                    httpClient,
                    mapper,
                    monitor,
                    settings);
        }
        return client;
    }

}
