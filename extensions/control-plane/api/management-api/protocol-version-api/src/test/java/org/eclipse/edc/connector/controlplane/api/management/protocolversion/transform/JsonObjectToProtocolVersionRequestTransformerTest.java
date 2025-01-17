/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.connector.controlplane.api.management.protocolversion.transform;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.protocolversion.spi.ProtocolVersionRequest;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.controlplane.protocolversion.spi.ProtocolVersionRequest.PROTOCOL_VERSION_REQUEST_COUNTER_PARTY_ADDRESS;
import static org.eclipse.edc.connector.controlplane.protocolversion.spi.ProtocolVersionRequest.PROTOCOL_VERSION_REQUEST_COUNTER_PARTY_ID;
import static org.eclipse.edc.connector.controlplane.protocolversion.spi.ProtocolVersionRequest.PROTOCOL_VERSION_REQUEST_PROTOCOL;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.mockito.Mockito.mock;

class JsonObjectToProtocolVersionRequestTransformerTest {

    private final TransformerContext context = mock();
    private final JsonObjectToProtocolVersionRequestTransformer transformer = new JsonObjectToProtocolVersionRequestTransformer();

    @Test
    void types() {
        assertThat(transformer.getInputType()).isEqualTo(JsonObject.class);
        assertThat(transformer.getOutputType()).isEqualTo(ProtocolVersionRequest.class);
    }

    @Test
    void transform() {
        var json = Json.createObjectBuilder()
                .add(TYPE, ProtocolVersionRequest.PROTOCOL_VERSION_REQUEST_TYPE)
                .add(PROTOCOL_VERSION_REQUEST_PROTOCOL, "protocol")
                .add(PROTOCOL_VERSION_REQUEST_COUNTER_PARTY_ID, "counterPartyId")
                .add(PROTOCOL_VERSION_REQUEST_COUNTER_PARTY_ADDRESS, "http://provider/url")
                .build();

        var result = transformer.transform(json, context);

        assertThat(result).isNotNull();
        assertThat(result.getProtocol()).isEqualTo("protocol");
        assertThat(result.getCounterPartyAddress()).isEqualTo("http://provider/url");
        assertThat(result.getCounterPartyId()).isEqualTo("counterPartyId");
    }

}
