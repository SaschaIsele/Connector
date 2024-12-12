/*
 * Copyright (c) 2023 ZF Friedrichshafen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesLoginResponsePayload;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.io.IOException;

public class HashicorpVaultKubernetesAuthClientTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void loginWithKubernetesAuth() throws IOException {
        // prepare
        String vaultUrl = "https://mock.url";
        String kubernetesRole = "role";
        String kubernetesServiceAccountToken = "serviceAccountToken";
        EdcHttpClient httpClient = Mockito.mock(EdcHttpClient.class);
        HashicorpVaultKubernetesAuthClient authClient = new HashicorpVaultKubernetesAuthClient(vaultUrl, httpClient, objectMapper);

        Response response = Mockito.mock(Response.class);
        ResponseBody body = Mockito.mock(ResponseBody.class);
        HashicorpVaultKubernetesLoginResponsePayload payload = new HashicorpVaultKubernetesLoginResponsePayload();

        Mockito.when(httpClient.execute(Mockito.any(Request.class))).thenReturn(response);
        Mockito.when(response.code()).thenReturn(200);
        Mockito.when(response.body()).thenReturn(body);
        Mockito.when(body.string()).thenReturn(payload.toString());

        // invoke
        Result<HashicorpVaultKubernetesLoginResponsePayload> result =
            authClient.loginWithKubernetesAuth(kubernetesRole, kubernetesServiceAccountToken);

        // verify
        Assertions.assertNotNull(result);
        Mockito.verify(httpClient, Mockito.times(1))
            .execute(
                Mockito.argThat(
                    request ->
                        request.method().equalsIgnoreCase("POST")
                            && request.url().encodedPath().endsWith("v1%2Fauth%2Fkubernetes%2Flogin")));
    }

}
