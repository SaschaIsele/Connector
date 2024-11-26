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

package org.eclipse.edc.vault.hashicorp.auth;

import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuth;
import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuthRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.edc.vault.hashicorp.HashicorpVaultExtension.VAULT_AUTH_DEFAULT;

public class HashicorpVaultAuthRegistryImpl implements HashicorpVaultAuthRegistry {

    private final Map<String, HashicorpVaultAuth> services = new HashMap<>();

    public HashicorpVaultAuthRegistryImpl(String fallbackToken) {
        services.put(VAULT_AUTH_DEFAULT, new SimpleTokenAuth(fallbackToken));
    }

    @Override
    public void register(String method, HashicorpVaultAuth service) {
        services.put(method, service);
    }

    @Override
    public @NotNull HashicorpVaultAuth resolve(String method) {
        return services.get(method);
    }

    @Override
    public boolean hasService(String method) {
        return services.containsKey(method);
    }
}
