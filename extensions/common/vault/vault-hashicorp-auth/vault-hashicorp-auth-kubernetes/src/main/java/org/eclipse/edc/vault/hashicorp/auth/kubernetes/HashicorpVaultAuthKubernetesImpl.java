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

package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuth;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesLoginResponsePayload;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesSettings.DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH;

public class HashicorpVaultAuthKubernetesImpl implements HashicorpVaultAuth {

    private String vaultToken;

    private final String vaultKubernetesAuthRole;
    private final HashicorpVaultKubernetesAuthClient authClient;
    private final String serviceAccountToken;
    private final Clock clock;
    private final Monitor monitor;

    public HashicorpVaultAuthKubernetesImpl(@NotNull String vaultKubernetesAuthRole,
                                        @NotNull HashicorpVaultKubernetesAuthClient authClient,
                                        @NotNull FileUtil fileUtil,
                                        @NotNull Clock clock,
                                        @NotNull Monitor monitor) {
        this.vaultKubernetesAuthRole = vaultKubernetesAuthRole;
        this.authClient = authClient;
        this.clock = clock;
        this.monitor = monitor;

        try {
            this.serviceAccountToken = fileUtil.readContentsFromFile(DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH);
        } catch (IOException ex) {
            monitor.warning(String.format("Failed reading service account token from local path [%s]", DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH));
        }
    }

    public LocalDateTime login() {
        Result<HashicorpVaultKubernetesLoginResponsePayload> res =
                authClient.loginWithKubernetesAuth(vaultKubernetesAuthRole, serviceAccountToken);

        if (res.failed()) {
            monitor.warning(String.format("Failed login with Kubernetes Service Account Token: (%s)", res.getFailureDetail()));
        }

        this.vaultToken = res.getContent().getAuth().getClientToken();

        //return the estimated timestamp for token expiry
        return LocalDateTime.now(clock).plus(res.getContent().getAuth().getLeaseDuration(), ChronoUnit.SECONDS);
    }

    @Override
    public String vaultToken() {
        return vaultToken;
    }

}
