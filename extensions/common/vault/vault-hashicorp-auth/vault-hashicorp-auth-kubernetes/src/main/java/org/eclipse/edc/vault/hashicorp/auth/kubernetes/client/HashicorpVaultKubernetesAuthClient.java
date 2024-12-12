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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class HashicorpVaultKubernetesAuthClient {

    @NotNull private final String vaultUrl;
    @NotNull private final EdcHttpClient httpClient;
    @NotNull private final ObjectMapper objectMapper;

    private static final MediaType MEDIA_TYPE_APPLICATION_JSON = MediaType.get("application/json");
    private static final String VAULT_REQUEST_HEADER = "X-Vault-Request";
    public static final String CALL_UNSUCCESSFUL_ERROR_TEMPLATE = "Call unsuccessful: %s";

    public HashicorpVaultKubernetesAuthClient(@NotNull String vaultUrl, @NotNull EdcHttpClient httpClient, @NotNull ObjectMapper objectMapper) {
        this.vaultUrl = vaultUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public Result<HashicorpVaultKubernetesLoginResponsePayload> loginWithKubernetesAuth(String authRole, String serviceAccountToken) {
        HttpUrl requestUrl = getKubernetesAuthLoginUrl();
        Headers headers = getHeaders();
        HashicorpVaultKubernetesLoginRequestPayload requestPayload = HashicorpVaultKubernetesLoginRequestPayload.Builder.newInstance()
                .role(authRole)
                .jwt(serviceAccountToken)
                .build();

        Request request = new Request.Builder()
                .url(requestUrl)
                .headers(headers)
                .post(createRequestBody(objectMapper, requestPayload))
                .build();

        try (Response response = httpClient.execute(request)) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                HashicorpVaultKubernetesLoginResponsePayload responsePayload =
                        objectMapper.readValue(responseBody, HashicorpVaultKubernetesLoginResponsePayload.class);

                return Result.success(responsePayload);
            } else {
                return Result.failure(String.format(CALL_UNSUCCESSFUL_ERROR_TEMPLATE, response.code()));
            }
        } catch (IOException e) {
            return Result.failure(e.getMessage());
        }
    }

    @NotNull
    private Headers getHeaders() {
        return new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true)).build();
    }

    private RequestBody createRequestBody(ObjectMapper objectMapper, Object requestPayload) {
        String jsonRepresentation;
        try {
            jsonRepresentation = objectMapper.writeValueAsString(requestPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return RequestBody.create(jsonRepresentation, MEDIA_TYPE_APPLICATION_JSON);
    }

    private HttpUrl getKubernetesAuthLoginUrl() {
        return Objects.requireNonNull(HttpUrl.parse(vaultUrl))
                .newBuilder()
                .addPathSegment("v1/auth/kubernetes/login")
                .build();
    }

}
