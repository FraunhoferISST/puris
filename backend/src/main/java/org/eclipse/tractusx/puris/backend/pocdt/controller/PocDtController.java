/*
 * Copyright (c) 2023, 2024 Volkswagen AG
 * Copyright (c) 2023, 2024 Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V.
 * (represented by Fraunhofer ISST)
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.puris.backend.pocdt.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.common.edc.logic.dto.datatype.DT_ApiMethodEnum;
import org.eclipse.tractusx.puris.backend.common.edc.logic.service.EdcAdapterService;
import org.eclipse.tractusx.puris.backend.common.edc.logic.util.EdcRequestBodyBuilder;
import org.eclipse.tractusx.puris.backend.pocdt.logic.service.PocDtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class contains REST endpoints for the Proof-of-Concept Digital Twin
 */
@RestController
@RequestMapping("pocDt")
@Slf4j
public class PocDtController {

    @Autowired
    private PocDtService pocDtService;
    @Autowired
    private EdcAdapterService edcAdapter;
    @Autowired
    private EdcRequestBodyBuilder edcRequestBodyBuilder;

    @PostMapping("/registerDtr1")
    @Operation(description = "Registers Digital Twin Registry as an asset in EDC for participant 1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "500", description = "Internal Server error.")
    })
    public ResponseEntity<?> registerDtr1() {
        boolean response = edcAdapter.registerAsset(edcRequestBodyBuilder.buildDigitalTwinRegistryAssetBody(4243));
        if(response)
        {
            log.info("Registration of DTR (4243) in edc successful");
        }
        else
        {
            log.info("Error while registration of DTR (4243) in edc");
        }
        boolean response2 = edcAdapter.createContractDefinitionForDTR("BPNL4444444444XX", 4243);
        if(response2)
        {
            log.info("ContractDefinition of DTR (4243) in edc successful");
        }
        else
        {
            log.info("Error while creating ContractDefinition of DTR (4243) in edc");
        }
        boolean response3 = pocDtService.registerAAS(4243);
        if(response3)
        {
            log.info("AAS successfully registered in DTR (4243)");
        }
        else
        {
            log.info("Error while registering AAS in DTR (4243)");
        }
        return new ResponseEntity<>("DTR1 successfully registered", HttpStatusCode.valueOf(200));
    }

    @PostMapping("/registerDtr2")
    @Operation(description = "Registers Digital Twin Registry as an asset in EDC for participant 2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "500", description = "Internal Server error.")
    })
    public ResponseEntity<?> registerDtr2() {
        boolean response = edcAdapter.registerAsset(edcRequestBodyBuilder.buildDigitalTwinRegistryAssetBody(4244));
        if(response)
        {
            log.info("Registration of DTR (4244) in edc successful");
        }
        else
        {
            log.info("Error while registration of DTR (4244) in edc");
        }
        boolean response2 = edcAdapter.createContractDefinitionForDTR("BPNL1234567890ZZ", 4244);
        if(response2)
        {
            log.info("ContractDefinition of DTR (4244) in edc successful");
        }
        else
        {
            log.info("Error while creating ContractDefinition of DTR (4244) in edc");
        }
        boolean response3 = pocDtService.registerAAS(4244);
        if(response3)
        {
            log.info("AAS successfully registered in DTR (4244)");
        }
        else
        {
            log.info("Error while registering AAS in DTR (4244)");
        }
        return new ResponseEntity<>("DTR2 successfully registered",HttpStatusCode.valueOf(200));
    }

    @GetMapping("/submodel/value")
    @Operation(description = "Returns a submodel for dDTR")
    public ResponseEntity<JsonNode> getSubmodel() {
        return new ResponseEntity<>(pocDtService.buildTestSubmodel(), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/showAAS")
    @Operation(description = "Returns an example AAS for dDTR")
    public ResponseEntity<JsonNode> getAAS() {
        return new ResponseEntity<>(pocDtService.buildAAS(4243), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/registerSubmodel")
    @Operation(description = "Registers submodel at dDTR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "500", description = "Internal Server error.")
    })
    public ResponseEntity<?> registerSubmodel() {
        return new ResponseEntity<>("Not implemented yet.", HttpStatusCode.valueOf(200));
    }
}
