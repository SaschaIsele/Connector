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
public class HashicorpVaultKubernetesLoginRequestPayload {
    @JsonProperty("jwt")
    private String jwt;
    @JsonProperty("role")
    private String role;

    public HashicorpVaultKubernetesLoginRequestPayload() {
    }

    public HashicorpVaultKubernetesLoginRequestPayload(String jwt, String role) {
        this.jwt = jwt;
        this.role = role;
    }

    public static class Builder {
        private final HashicorpVaultKubernetesLoginRequestPayload payload;

        private Builder() {
            payload = new HashicorpVaultKubernetesLoginRequestPayload();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder jwt(String jwt) {
            this.payload.jwt = jwt;
            return this;
        }

        public Builder role(String role) {
            this.payload.role = role;
            return this;
        }

        public HashicorpVaultKubernetesLoginRequestPayload build() {
            return this.payload;
        }
    }
}
