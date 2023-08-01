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
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.tractusx.puris.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.common.api.domain.model.ProductStockRequest;
import org.eclipse.tractusx.puris.backend.common.api.domain.model.datatype.DT_RequestStateEnum;
import org.eclipse.tractusx.puris.backend.common.api.domain.model.datatype.DT_UseCaseEnum;
import org.eclipse.tractusx.puris.backend.common.api.logic.dto.MessageContentErrorDto;
import org.eclipse.tractusx.puris.backend.common.api.logic.dto.MessageHeaderDto;
import org.eclipse.tractusx.puris.backend.common.api.logic.service.RequestService;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Material;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.MaterialPartnerRelation;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Partner;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialPartnerRelationService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.PartnerService;
import org.eclipse.tractusx.puris.backend.stock.domain.model.MaterialStock;
import org.eclipse.tractusx.puris.backend.stock.domain.model.PartnerProductStock;
import org.eclipse.tractusx.puris.backend.stock.domain.model.ProductStock;
import org.eclipse.tractusx.puris.backend.stock.logic.adapter.ApiMarshallingService;
import org.eclipse.tractusx.puris.backend.stock.logic.adapter.ProductStockSammMapper;
import org.eclipse.tractusx.puris.backend.stock.logic.dto.ProductStockDto;
import org.eclipse.tractusx.puris.backend.stock.logic.dto.ProductStockRequestDto;
import org.eclipse.tractusx.puris.backend.stock.logic.dto.ProductStockRequestForMaterialDto;
import org.eclipse.tractusx.puris.backend.stock.logic.dto.ProductStockResponseDto;
import org.eclipse.tractusx.puris.backend.stock.logic.dto.samm.ProductStockSammDto;
import org.eclipse.tractusx.puris.backend.stock.logic.service.MaterialStockService;
import org.eclipse.tractusx.puris.backend.stock.logic.service.PartnerProductStockService;
import org.eclipse.tractusx.puris.backend.stock.logic.service.ProductStockService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DataInjectionCommandLineRunner implements CommandLineRunner {

    @Autowired
    private ModelMapper modelMapper;


    @Autowired
    private MaterialService materialService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private MaterialPartnerRelationService mprService;

    @Autowired
    private MaterialStockService materialStockService;

    @Autowired
    private ProductStockService productStockService;

    @Autowired
    private PartnerProductStockService partnerProductStockService;

    @Autowired
    private ProductStockSammMapper productStockSammMapper;

    @Autowired
    private RequestService requestService;


    @Value("${puris.demonstrator.role}")
    private String demoRole;

    @Autowired
    private ApiMarshallingService apiMarshallingService;

    private ObjectMapper objectMapper;

    private final String semiconductorMatNbrCustomer = "MNR-7307-AU340474.002";
    private final String semiconductorMatNbrSupplier = "MNR-8101-ID146955.001";

    public DataInjectionCommandLineRunner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void run(String... args) throws Exception {

        log.info("Creating setup for " + demoRole.toUpperCase());
        if (demoRole.equals("supplier")) {
            setupSupplierRole();
        } else if (demoRole.equals(("customer"))) {
            setupCustomerRole();
            createRequest();
        } else {
            log.info("No role specific setup was created");
        }
    }

    /**
     * Generates an initial set of data for a customer within the demonstration context. 
     * @throws JsonProcessingException
     */
    private void setupCustomerRole() throws JsonProcessingException {
        Partner supplierPartner = createAndGetSupplierPartner();
        Material semiconductorMaterial = getNewSemiconductorMaterialForCustomer();

        semiconductorMaterial = materialService.create(semiconductorMaterial);
        log.info(String.format("Created material: %s", semiconductorMaterial));
        List<Material> materialsFound = materialService.findAllMaterials();
        log.info(String.format("Found Material: %s", materialsFound));
        log.info(String.format("UUID of supplier partner: %s", supplierPartner.getUuid()));
        supplierPartner = partnerService.findByUuid(supplierPartner.getUuid());
        log.info(String.format("Found supplier partner: %s", supplierPartner));


        MaterialPartnerRelation semiconductorPartnerRelation = new MaterialPartnerRelation(semiconductorMaterial,
            supplierPartner, semiconductorMatNbrSupplier, true, false);
        mprService.create(semiconductorPartnerRelation);
        semiconductorPartnerRelation = mprService.find(semiconductorMaterial, supplierPartner);
        log.info("Found Relation: " + semiconductorPartnerRelation);

        // customer + material
        Partner nonScenarioCustomer = createAndGetNonScenarioCustomer();
        Material centralControlUnitEntity = getNewCentralControlUnitMaterial();
        centralControlUnitEntity = materialService.create(centralControlUnitEntity);
        log.info(String.format("Created Product: %s", centralControlUnitEntity));

        MaterialPartnerRelation ccuPartnerRelation = new MaterialPartnerRelation(centralControlUnitEntity,
            nonScenarioCustomer, "MNR-4177-C", false, true);
        ccuPartnerRelation = mprService.create(ccuPartnerRelation);
        log.info("Found Relation: " + ccuPartnerRelation);

        log.info("All stored Relations: " + mprService.findAll());

        List<Material> productsFound = materialService.findAllProducts();
        log.info(String.format("Found Products: %s", productsFound));

//        centralControlUnitEntity =
//                materialService.findProductByMaterialNumberCustomer(centralControlUnitEntity.getMaterialNumberCustomer());
//        log.info(String.format("Found product by materialNumber customer: %s",
//                centralControlUnitEntity));
//        nonScenarioCustomer = partnerService.findByUuid(nonScenarioCustomer.getUuid());
//        log.info(String.format("Relationship to product: %s",
//                nonScenarioCustomer.getOrdersProducts()));
//
//        centralControlUnitEntity =
//                materialService.findProductByMaterialNumberCustomer(centralControlUnitEntity.getMaterialNumberCustomer());
//        log.info(String.format("Found product by materialNumber customer: %s",
//                centralControlUnitEntity));
//
//        Material existingMaterial =
//                materialService.findByUuid(semiconductorMaterial.getUuid());
//        log.info(String.format("Found existingMaterial by uuid: %s",
//                existingMaterial));
//
//        Material existingProduct =
//                materialService.findProductByMaterialNumberCustomer(centralControlUnitEntity.getMaterialNumberCustomer());
//        log.info(String.format("Found existingProduct by customer number: %s",
//                existingProduct));
//
//        List<Material> existingProducts =
//                materialService.findAllProducts();
//        log.info(String.format("Found existingProducts by product flag true: %s",
//                existingProducts));
//
//        log.info(String.format("Relationship centralControlUnitEntity -> orderedByPartners: %s",
//                centralControlUnitEntity.getOrderedByPartners().toString()));

        // Create Material Stock
        MaterialStock materialStockEntity = new MaterialStock(
                semiconductorMaterial,
                20,
                "BPNS4444444444XX",
                new Date()
        );
        materialStockEntity = materialStockService.create(materialStockEntity);
        log.info(String.format("Created materialStock: %s", materialStockEntity));
//        List<MaterialStock> foundMaterialStocks =
//                materialStockService.findAllByMaterialNumberCustomer(semiconductorMaterial.getMaterialNumberCustomer());
//        log.info(String.format("Found materialStock: %s", foundMaterialStocks));

        // Create PartnerProductStock
        semiconductorMaterial = materialService.findByOwnMaterialNumber(semiconductorMaterial.getOwnMaterialNumber());
        PartnerProductStock partnerProductStockEntity = new PartnerProductStock(
                semiconductorMaterial,
                20,
                supplierPartner.getSiteBpns(),
                new Date(),
                supplierPartner
        );
        log.info(String.format("Created partnerProductStock: %s", partnerProductStockEntity));
        partnerProductStockEntity = partnerProductStockService.create(partnerProductStockEntity);
        ProductStockSammDto productStockSammDto = productStockSammMapper.toSamm(partnerProductStockEntity);
        log.info("SAMM-DTO:\n" + objectMapper.writeValueAsString(productStockSammDto));
    }
    /**
     * Generates an initial set of data for a supplier within the demonstration context. 
     */
    private void setupSupplierRole() {
        Partner customerPartner = createAndGetCustomerPartner();
        Material semiconductorMaterial = getNewSemiconductorMaterialForSupplier();
//        semiconductorMaterial.addPartnerToOrderedByPartners(customerPartner);
        semiconductorMaterial = materialService.create(semiconductorMaterial);
        log.info(String.format("Created product: %s", semiconductorMaterial));

        MaterialPartnerRelation semiconductorPartnerRelation = new MaterialPartnerRelation(semiconductorMaterial,
            customerPartner, semiconductorMatNbrCustomer, false, true);
        semiconductorPartnerRelation = mprService.create(semiconductorPartnerRelation);

        log.info("Created Relation " + semiconductorPartnerRelation);

        semiconductorPartnerRelation = mprService.find(semiconductorMaterial, customerPartner);

        log.info("Found Relation " + semiconductorPartnerRelation);

        List<Material> materialsFound = materialService.findAllProducts();
        log.info(String.format("Found product: %s", materialsFound));
        log.info(String.format("Found customer partner: %s", customerPartner));
//        log.info(String.format("Relationship to material: %s", customerPartner.getOrdersProducts()));

        ProductStock productStockEntity = new ProductStock(
                semiconductorMaterial,
                20,
                "BPNS1234567890ZZ",
                new Date(),
                customerPartner
        );
        productStockEntity = productStockService.create(productStockEntity);
        log.info(String.format("Created productStock: %s", productStockEntity.toString()));
//        List<ProductStock> foundProductStocks =
//                productStockService
//                        .findAllByMaterialNumberCustomerAndAllocatedToCustomerBpnl(
//                                semiconductorMaterial.getMaterialNumberCustomer(),
//                                customerPartner.getBpnl());
//        log.info(String.format("Found productStocks by material number and allocated to customer " +
//                "bpnl: %s", foundProductStocks));
    }


    /**
     * creates a new customer Partner entity, stores it to
     * the database and returns this entity. 
     * @return a reference to the newly created customer
     */
    private Partner createAndGetCustomerPartner() {
        Partner customerPartnerEntity = new Partner(
                "Scenario Customer",
                "http://sokrates-controlplane:8084/api/v1/ids",
                "BPNL4444444444XX",
                "BPNS4444444444XX"
        );
        customerPartnerEntity = partnerService.create(customerPartnerEntity);
        log.info(String.format("Created customer partner: %s", customerPartnerEntity));
        customerPartnerEntity = partnerService.findByUuid(customerPartnerEntity.getUuid());
        log.info(String.format("Found customer partner: %s", customerPartnerEntity));
        return customerPartnerEntity;
    }

    /**
     * creates a new supplier Partner entity, stores it to
     * the database and returns this entity. 
     * @return a reference to the newly created supplier
     */
    private Partner createAndGetSupplierPartner() {
        Partner supplierPartnerEntity = new Partner(
                "Scenario Supplier",
                "http://plato-controlplane:8084/api/v1/ids",
                "BPNL1234567890ZZ",
                "BPNS1234567890ZZ"
        );
        supplierPartnerEntity = partnerService.create(supplierPartnerEntity);
        log.info(String.format("Created supplier partner: %s", supplierPartnerEntity));
        supplierPartnerEntity = partnerService.findByUuid(supplierPartnerEntity.getUuid());
        log.info(String.format("Found supplier partner: %s", supplierPartnerEntity));
        return supplierPartnerEntity;
    }

    /**
     * creates a new (non-scenario) customer entity, stores
     * it to the database and returns this entity. 
     * @return a reference to the newly created non-scenario customer
     */
    private Partner createAndGetNonScenarioCustomer() {
        Partner nonScenarioCustomer = new Partner(
                "Non-Scenario Customer",
                "(None Provided!)>",
                "BPNL2222222222RR",
                "BPNL2222222222RR"
        );
        nonScenarioCustomer = partnerService.create(nonScenarioCustomer);
        log.info(String.format("Created non-scenario customer partner: %s", nonScenarioCustomer));
        nonScenarioCustomer = partnerService.findByUuid(nonScenarioCustomer.getUuid());
        log.info(String.format("Found non-scenario customer partner: %s", nonScenarioCustomer));
        return nonScenarioCustomer;
    }

    private Material getNewSemiconductorMaterialForSupplier() {
        Material material = new Material();
        material.setOwnMaterialNumber(semiconductorMatNbrSupplier);
        material.setProductFlag(true);
        material.setName("semiconductor");
        return material;
    }

    private Material getNewSemiconductorMaterialForCustomer() {
        Material material = new Material();
        material.setOwnMaterialNumber(semiconductorMatNbrCustomer);
        material.setMaterialFlag(true);
        material.setName("semiconductor");
        return material;
    }

    /**
     * creates a new central control unit Material object. 
     * Note: this object is not yet stored to the database 
     * @return a reference to the newly created central control unit material
     */
    private Material getNewCentralControlUnitMaterial() {
        Material material = new Material();
        material.setOwnMaterialNumber("MNR-4177-S");
        material.setProductFlag(true);
        material.setName("central control unit");
        return material;
    }

    private void createRequest() throws JsonProcessingException {
        MessageHeaderDto messageHeaderDto = new MessageHeaderDto();
        messageHeaderDto.setRequestId(UUID.fromString("4979893e-dd6b-43db-b732-6e48b4ba35b3"));
        messageHeaderDto.setRespondAssetId("product-stock-response-api");
        messageHeaderDto.setContractAgreementId("some cid");
        messageHeaderDto.setSender("BPNL1234567890ZZ");
        messageHeaderDto.setSenderEdc("http://plato-controlplane:8084/api/v1/ids");
        messageHeaderDto.setReceiver("BPNL4444444444XX");
        messageHeaderDto.setUseCase(DT_UseCaseEnum.PURIS);
        messageHeaderDto.setCreationDate(new Date());

        log.info(objectMapper.writeValueAsString(messageHeaderDto));

        List<ProductStockRequestForMaterialDto> messageContentDtos = new ArrayList<>();

        ProductStockRequestForMaterialDto messageContentDto = new ProductStockRequestForMaterialDto();
        messageContentDto.setMaterialNumberCustomer("CU-MNR");
        messageContentDto.setMaterialNumberSupplier("SU-MNR");
        messageContentDtos.add(messageContentDto);
        messageContentDto = new ProductStockRequestForMaterialDto();
        messageContentDto.setMaterialNumberCustomer("OtherCU-MNR");
        messageContentDto.setMaterialNumberSupplier("OtherSU-MNR");
        messageContentDtos.add(messageContentDto);

        ProductStockRequestDto requestDto = new ProductStockRequestDto(
                DT_RequestStateEnum.RECEIPT,
                messageHeaderDto,
                messageContentDtos
        );
        ProductStockRequest createdProductStockRequest = requestService.createRequest(modelMapper.map(requestDto,
            ProductStockRequest.class));
        log.info(String.format("Created Request: %s", createdProductStockRequest));
        log.info(createdProductStockRequest.getPayload().get(0).getClass().toString());

        log.info("Testing ApiMarshallingService:");
        String transformationTest = apiMarshallingService.transformProductStockRequest(requestDto);
        log.info("marshalled request to be sent:\n" + transformationTest);

        ProductStockRequestDto productStockRequestDto = apiMarshallingService.transformToProductStockRequestDto(transformationTest);
        log.info("unmarshalled the same request as productStockRequestDto: \n" + productStockRequestDto.toString());

        String sampleResponse = "{\n" +
            "  \"header\" : {\n" +
            "    \"requestId\" : \"37be1c8e-e2c3-4fbc-848f-9ee576cecc9f\",\n" +
            "    \"respondAssetId\" : null,\n" +
            "    \"creationDate\" : \"2023-07-27T10:56:43.116+00:00\",\n" +
            "    \"senderEdc\" : \"http://plato-controlplane:8084/api/v1/ids\",\n" +
            "    \"sender\" : \"BPNL1234567890ZZ\",\n" +
            "    \"receiver\" : \"BPNL4444444444XX\",\n" +
            "    \"useCase\" : \"PURIS\",\n" +
            "    \"contractAgreementId\" : \"product-stock-response-api:c51b78d3-06fa-4539-8d3a-2a0fb19780ba\"\n" +
            "  },\n" +
            "  \"content\" : {\n" +
            "    \"productStock\" : [ {\n" +
            "      \"positions\" : [ {\n" +
            "        \"orderPositionReference\" : null,\n" +
            "        \"lastUpdatedOnDateTime\" : 1690455395379,\n" +
            "        \"allocatedStocks\" : [ {\n" +
            "          \"quantityOnAllocatedStock\" : {\n" +
            "            \"quantityNumber\" : 20.0,\n" +
            "            \"measurementUnit\" : \"unit:piece\"\n" +
            "          },\n" +
            "          \"supplierStockLocationId\" : {\n" +
            "            \"locationIdType\" : \"BPNS\",\n" +
            "            \"locationId\" : \"BPNS1234567890ZZ\"\n" +
            "          }\n" +
            "        } ]\n" +
            "      } ],\n" +
            "      \"materialNumberCustomer\" : \"MNR-7307-AU340474.002\",\n" +
            "      \"materialNumberCatenaX\" : {\n" +
            "        \"empty\" : true,\n" +
            "        \"present\" : false\n" +
            "      },\n" +
            "      \"materialNumberSupplier\" : {\n" +
            "        \"empty\" : false,\n" +
            "        \"present\" : true\n" +
            "      }\n" +
            "    } ]\n" +
            "  }\n" +
            "}\n";
        ProductStockResponseDto productStockResponseDto = apiMarshallingService.transformToProductStockResponseDto(sampleResponse);
        // insert a MessageContentErrorDto
        MessageContentErrorDto messageContentErrorDto = new MessageContentErrorDto();
        messageContentErrorDto.setMaterialNumberCustomer("Sample MaterialNumber");
        messageContentErrorDto.setError("Sample Error");
        messageContentErrorDto.setMessage("Sample Error Message");
        productStockResponseDto.getPayload().add(messageContentErrorDto);
        log.info(productStockResponseDto.toString());
        String productStockResponseString = apiMarshallingService.transformProductStockResponse(productStockResponseDto);
        log.info("marshalled sample response: \n" + productStockResponseString);
        productStockResponseDto = apiMarshallingService.transformToProductStockResponseDto(productStockResponseString);
        log.info("unmarshalled header: \n" + productStockResponseDto.getHeader());
        log.info("unmarshalled content: \n" + productStockResponseDto.getPayload());


    }
}
