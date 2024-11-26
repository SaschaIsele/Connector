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
 *       Mercedes-Benz Tech Innovation GmbH - Implement automatic Hashicorp Vault token renewal
 *       Cofinity-X GmbH - Authentication refactoring
 *
 */

package org.eclipse.edc.vault.hashicorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.vault.hashicorp.auth.HashicorpVaultAuthRegistryImpl;
import org.eclipse.edc.vault.hashicorp.client.HashicorpVaultClient;
import org.eclipse.edc.vault.hashicorp.client.HashicorpVaultSettings;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Extension(value = HashicorpVaultExtension.NAME)
public class HashicorpVaultExtension implements ServiceExtension {
    public static final String NAME = "Hashicorp Vault";

    @Inject
    private EdcHttpClient httpClient;

    @Configuration
    private HashicorpVaultSettings config;

    private HashicorpVaultClient client;
    private Monitor monitor;
    private HashicorpVaultAuthRegistryImpl registry;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public HashicorpVaultClient hashicorpVaultClient() {
        if (client == null) {
            // the default type manager cannot be used as the Vault is a primordial service loaded at boot
            var mapper = new ObjectMapper();
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

            client = new HashicorpVaultClient(
                    httpClient,
                    mapper,
                    monitor,
                    registry,
                    config);
        }
        return client;
    }

    @Provider
    public HashicorpVaultAuthRegistryImpl hashicorpVaultAuthRegistry() {
        return getRegistry();
    }

    @Provider
    public Vault hashicorpVault() {
        return new HashicorpVault(hashicorpVaultClient(), monitor);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        registry = getRegistry();
    }

    private HashicorpVaultAuthRegistryImpl getRegistry() {
        if (registry == null) {
            registry = new HashicorpVaultAuthRegistryImpl(config.getFallbackToken());
        }
        return registry;
    }

}
