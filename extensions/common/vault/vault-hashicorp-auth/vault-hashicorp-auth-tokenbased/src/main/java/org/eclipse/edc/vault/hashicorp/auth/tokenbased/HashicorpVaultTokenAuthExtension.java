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
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
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

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    @Inject
    private HashicorpVaultAuthRegistry registry;

    @Configuration
    private HashicorpVaultTokenAuthSettings tokenConfig;

    private HashicorpVaultTokenAuthClient client;
    private HashicorpVaultTokenRenewTask tokenRenewalTask;
    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        registry.register("token-based", new HashicorpVaultAuthTokenImpl(tokenConfig.token()));
        tokenRenewalTask = new HashicorpVaultTokenRenewTask(
                NAME,
                executorInstrumentation,
                hashicorpVaultTokenAuthClient(),
                tokenConfig.renewBuffer(),
                monitor);
    }

    @Override
    public void start() {
        if (tokenConfig.scheduledTokenRenewEnabled()) {
            tokenRenewalTask.start();
        }
    }

    @Override
    public void shutdown() {
        if (tokenRenewalTask.isRunning()) {
            tokenRenewalTask.stop();
        }
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
                    tokenConfig);
        }
        return client;
    }

}
