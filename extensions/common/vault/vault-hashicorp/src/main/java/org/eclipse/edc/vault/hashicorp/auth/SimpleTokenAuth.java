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
 *       Cofinity-X GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.vault.hashicorp.auth;

import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuth;

public class SimpleTokenAuth implements HashicorpVaultAuth {
    private String vaultToken;

    SimpleTokenAuth(String vaultToken) {
        this.vaultToken = vaultToken;
    }

    @Override
    public String vaultToken() {
        return vaultToken;
    }
}
