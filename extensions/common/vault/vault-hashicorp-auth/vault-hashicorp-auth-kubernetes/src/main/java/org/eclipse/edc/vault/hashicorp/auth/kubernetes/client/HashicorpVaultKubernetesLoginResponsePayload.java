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

package org.eclipse.edc.vault.hashicorp.auth.kubernetes.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HashicorpVaultKubernetesLoginResponsePayload {
    @JsonProperty("auth")
    private Auth auth;

    public HashicorpVaultKubernetesLoginResponsePayload() {
    }

    public HashicorpVaultKubernetesLoginResponsePayload(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Auth {
        @JsonProperty("client_token")
        private String clientToken;

        @JsonProperty("lease_duration")
        private Long leaseDuration;

        public Auth() {}

        public Auth(String clientToken, Long leaseDuration) {
            this.clientToken = clientToken;
            this.leaseDuration = leaseDuration;
        }

        public String getClientToken() {
            return clientToken;
        }

        public Long getLeaseDuration() {
            return leaseDuration;
        }
    }
}
