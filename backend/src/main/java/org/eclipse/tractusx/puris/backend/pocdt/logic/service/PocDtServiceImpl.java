/*
 * Copyright (c) 2023 Volkswagen AG
 * Copyright (c) 2023 Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V.
 * (represented by Fraunhofer ISST)
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.tractusx.puris.backend.pocdt.logic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.common.api.logic.service.VariablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PocDtServiceImpl implements PocDtService {

    private static final OkHttpClient CLIENT = new OkHttpClient();
    @Autowired
    private ObjectMapper MAPPER;
    @Autowired
    private VariablesService variablesService;

    private final String EDC_NAMESPACE = "https://w3id.org/edc/v0.0.1/ns/";
    private final String VOCAB_KEY = "@vocab";
    private final String ODRL_NAMESPACE = "http://www.w3.org/ns/odrl/2/";
    private final String CX_TAXO_NAMESPACE = "https://w3id.org/catenax/taxonomy#";
    private final String CX_COMMON_NAMESPACE = "https://w3id.org/catenax/ontology/common#";
    private final String CX_VERSION_KEY = "https://w3id.org/catenax/ontology/common#version";
    private final String CX_VERSION_NUMBER = "2.0.0";
    private final String DCT_NAMESPACE = "https://purl.org/dc/terms/";
    private final String PURL_TYPE_KEY = "https://purl.org/dc/terms/type";

    public Response sendPostRequest(JsonNode requestBody, String url) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());

        var request = new Request.Builder()
            .post(body)
            .url(urlBuilder.build())
            .header("Content-Type", "application/json")
            .build();
        return CLIENT.newCall(request).execute();
    }

    public Response sendGetRequest(String url, String bpn) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

        if(bpn.equals("NULL"))
        {
            var request = new Request.Builder()
                .get()
                .url(urlBuilder.build())
                .header("Content-Type", "application/json")
                .build();
            return CLIENT.newCall(request).execute();
        }
        else {
            var request = new Request.Builder()
                .get()
                .url(urlBuilder.build())
                .header("Content-Type", "application/json")
                .header("Edc-Bpn", bpn)
                .build();
            return CLIENT.newCall(request).execute();
        }
    }

    public boolean registerAAS(int port)
    {
        try {
            var url = "http://dtr-1:4243/api/v3.0/shell-descriptors";
            if(port == 4244)
            {
                url = "http://dtr-2:4243/api/v3.0/shell-descriptors";
            }
            var response = sendPostRequest(buildAAS(port), url);
            boolean result = response.isSuccessful();
            if (!result) {
                log.warn("False result while registering AAS to DTR \n" + response.body().string());
            }
            else
            {
                log.info("Register AAS successful: " + response.message());
            }
            response.body().close();
            return result;
        } catch (Exception e) {
            log.error("Failed to register AAS in DTR ", e);
            return false;
        }
    }

    public JsonNode buildAAS(int port)
    {
        var body = MAPPER.createObjectNode();

        var desc_de = MAPPER.createObjectNode();
        desc_de.put("language", "de");
        desc_de.put("text", "Deutsche Beschreibung");
        var desc_en = MAPPER.createObjectNode();
        desc_en.put("language", "en");
        desc_en.put("text", "English Description");

        var desc = MAPPER.createArrayNode();
        desc.add(desc_de);
        desc.add(desc_en);

        body.set("description", desc);

        var display_de = MAPPER.createObjectNode();
        display_de.put("language", "de");
        display_de.put("text", "Dies ist der Anzeige Name");

        var display = MAPPER.createArrayNode();
        display.add(display_de);

        body.set("displayName", display);

        var endpoint = MAPPER.createObjectNode();
        endpoint.put("interface", "interfaceNameExample");

        var info = MAPPER.createObjectNode();
        var bpn = "BPNL1234567890ZZ";
        if(port == 4243) {
            info.put("href", "http://localhost:8081/catena/pocDt/submodel/value");
            bpn = "BPNL4444444444XX";
        }
        else
        {
            info.put("href", "http://localhost:8082/catena/pocDt/submodel/value");
        }
        info.put("endpointProtocol", "http");

        var protocolVersion = MAPPER.createArrayNode();
        protocolVersion.add("1.0");

        info.set("endpointProtocolVersion", protocolVersion);
        info.put("subprotocol", "subprotocolExample");
        info.put("subprotocolBody", "subprotocolBodyExample");
        info.put("subprotocolBodyEncoding", "subprotocolBodyEncodingExample");

        var sec = MAPPER.createObjectNode();
        sec.put("type", "NONE");
        sec.put("key", "Security Attribute Key");
        sec.put("value", "Security Attribute Value");

        var secAtt = MAPPER.createArrayNode();
        secAtt.add(sec);

        info.set("securityAttributes", secAtt);

        endpoint.set("protocolInformation", info);

        var endpoints = MAPPER.createArrayNode();
        endpoints.add(endpoint);

        body.set("endpoints", endpoints);
        body.put("idShort", "shortIdAAS" + port);
        body.put("id", "longIdAAS" + port);

        var semId = MAPPER.createObjectNode();
        semId.put("type", "ExternalReference");

        var semIdKey = MAPPER.createObjectNode();
        semIdKey.put("type", "Property");
        semIdKey.put("value", bpn);

        var semIdKeys = MAPPER.createArrayNode();
        semIdKeys.add(semIdKey);

        semId.set("keys", semIdKeys);

        var specAssetId = MAPPER.createObjectNode();
        specAssetId.set("externalSubjectId", semId);
        specAssetId.put("name", "serialnr");
        specAssetId.put("value", "12345");

        var specAssetIds = MAPPER.createArrayNode();
        specAssetIds.add(specAssetId);

        body.set("specificAssetIds", specAssetIds);

        var submodel = MAPPER.createObjectNode();
        submodel.set("endpoints", endpoints);
        submodel.put("idShort", "shortIdSubmodel");
        submodel.put("id", "longIdSubmodel");
        submodel.set("semanticId", semId);
        submodel.set("description", desc);

        var submodels = MAPPER.createArrayNode();
        submodels.add(submodel);

        body.set("submodelDescriptors", submodels);

        return body;
    }

    public JsonNode buildTestSubmodel() {
        var body = MAPPER.createObjectNode();

        var id = MAPPER.createObjectNode();
        id.put("idType", "Iri");
        id.put("id", "https://catenax.io/ids/order/4711/orderStatus");
        body.set("identification", id);

        body.put("idShort", "testSubmodelShortId");
        body.put("manufacturerOrderId", "testManufacturerOrderId");
        body.put("customerOrderId", "testCustomerOrderId");

        var status = MAPPER.createObjectNode();
        status.put("requiredDateOfCompletion", "2023-03");
        status.put("manufacturerPartId", "123-0.740-3434-A");
        status.put("customerSiteBpns", "BPNS1234567890YY");

        var stocks = MAPPER.createObjectNode();

        var quantity = MAPPER.createObjectNode();
        quantity.put("quantityNumber", "2.5");
        quantity.put("measurementUnit", "litre");

        stocks.set("quantityOnAllocatedStock", quantity);
        stocks.put("manufacturerStockSiteBpns", "BPNS1234567890YY");

        status.set("allocatedStocks", stocks);
        status.put("positionId", "PositionId-01");

        var quantityOrdered = MAPPER.createObjectNode();
        quantityOrdered.put("quantityNumber", "2.5");
        quantityOrdered.put("measurementUnit", "litre");

        status.set("quantityOrdered", quantityOrdered);

        var total = MAPPER.createObjectNode();
        total.put("quantityNumber", "2.5");
        total.put("measurementUnit", "litre");

        status.set("totalAllocatedQuantityOnStock", total);

        body.set("orderPoitionStatuses", status);

        return body;
    }

    public JsonNode buildTestSubmodelAsset() {
        var body = MAPPER.createObjectNode();

        var context = MAPPER.createObjectNode();
        context.put(VOCAB_KEY, EDC_NAMESPACE);
        context.put("cx-common", CX_COMMON_NAMESPACE);
        context.put("cx-taxo", CX_TAXO_NAMESPACE);
        context.put("dct", DCT_NAMESPACE);
        context.put("aas-semantics", "https://admin-shell.io/aas/3/0/HasSemantics/");
        body.set("@context", context);

        body.put("@type", "Asset");
        body.put("@id", "TestSubmodelId");

        var propertiesObject = MAPPER.createObjectNode();
        var dctTypeObject = MAPPER.createObjectNode();
        dctTypeObject.put("@id", "cx-taxo:Submodel");
        propertiesObject.set("dct:type", dctTypeObject);
        propertiesObject.put("cx-common:version", "3.0");
        propertiesObject.put("asset:prop:type", "data.core.submodel");
        propertiesObject.put("description", "");
        var aasSemantics = MAPPER.createObjectNode();
        aasSemantics.put("@id", "urn:bamm:io.catenax.asset_tracker_links:1.0.0#AssetTrackerLinks");
        propertiesObject.set("aas-semantics:semanticId", aasSemantics);
        body.set("properties", propertiesObject);

        var dataAddress = MAPPER.createObjectNode();
        String url = "submodel url goes here";
        dataAddress.put("baseUrl", url);
        dataAddress.put("type", "HttpData");
        dataAddress.put("proxyPath", "true");
        dataAddress.put("proxyBody", "true");
        dataAddress.put("proxyMethod", "true");
        dataAddress.put("authKey", "x-api-key");
        dataAddress.put("authCode", variablesService.getApiKey());
        body.set("dataAddress", dataAddress);

        return body;
    }

    public JsonNode buildTestSubmodelAssetStockExchange() {
        var body = MAPPER.createObjectNode();

        var context = MAPPER.createObjectNode();
        context.put(VOCAB_KEY, EDC_NAMESPACE);
        context.put("cx-common", CX_COMMON_NAMESPACE);
        context.put("cx-taxo", CX_TAXO_NAMESPACE);
        context.put("dct", DCT_NAMESPACE);
        context.put("aas-semantics", "https://admin-shell.io/aas/3/0/HasSemantics/");
        body.set("@context", context);

        body.put("@id", "TestSubmodelId");

        var propertiesObject = MAPPER.createObjectNode();
        var dctTypeObject = MAPPER.createObjectNode();
        dctTypeObject.put("@id", "cx-taxo:Submodel");
        propertiesObject.set("dct:type", dctTypeObject);
        propertiesObject.put("cx-common:version", "3.0");
        var aasSemantics = MAPPER.createObjectNode();
        aasSemantics.put("@id", "urn:samm:io.catenax.item_stock:2.0.0#ItemStock");
        propertiesObject.set("aas-semantics:semanticId", aasSemantics);
        body.set("properties", propertiesObject);

        var privateProperties = MAPPER.createObjectNode();
        body.set("privateProperties", privateProperties);

        var dataAddress = MAPPER.createObjectNode();
        dataAddress.put("@type", "DataAddress");
        dataAddress.put("type", "HttpData");
        String url = "submodel url goes here";
        dataAddress.put("baseUrl", url);
        dataAddress.put("proxyQueryParams", "false");
        dataAddress.put("proxyBody", "false");
        dataAddress.put("proxyPath", "true");
        dataAddress.put("proxyMethod", "false");
        body.set("dataAddress", dataAddress);

        return body;
    }
}
