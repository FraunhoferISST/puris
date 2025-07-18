/*
 * Copyright (c) 2024 Volkswagen AG
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.puris.backend.delivery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.common.util.PatternStore;
import org.eclipse.tractusx.puris.backend.delivery.domain.model.OwnDelivery;
import org.eclipse.tractusx.puris.backend.delivery.domain.model.ReportedDelivery;
import org.eclipse.tractusx.puris.backend.delivery.logic.dto.DeliveryDto;
import org.eclipse.tractusx.puris.backend.delivery.logic.service.DeliveryRequestApiService;
import org.eclipse.tractusx.puris.backend.delivery.logic.service.OwnDeliveryService;
import org.eclipse.tractusx.puris.backend.delivery.logic.service.ReportedDeliveryService;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Material;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Partner;
import org.eclipse.tractusx.puris.backend.masterdata.logic.dto.PartnerDto;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialPartnerRelationService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.PartnerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("delivery")
@Slf4j
public class DeliveryController {
    @Autowired
    private OwnDeliveryService ownDeliveryService;

    @Autowired
    private ReportedDeliveryService reportedDeliveryService;

    @Autowired
    private DeliveryRequestApiService deliveryRequestApiService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private MaterialPartnerRelationService mprService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Validator validator;

    private final Pattern materialPattern = PatternStore.NON_EMPTY_NON_VERTICAL_WHITESPACE_PATTERN;

    @Autowired
    private ExecutorService executorService;

    @GetMapping()
    @ResponseBody
    @Operation(summary = "Get all planned deliveries for the given Material",
        description = "Get all planned deliveries for the given material number. Optionally a bpns and partner bpnl can be provided to filter the deliveries further.")
    public List<DeliveryDto> getAllDeliveries(@Parameter(description = "encoded in base64") String ownMaterialNumber, Optional<String> bpns, Optional<String> bpnl) {
        ownMaterialNumber = new String(Base64.getDecoder().decode(ownMaterialNumber));
        Material material = materialService.findByOwnMaterialNumber(ownMaterialNumber);
        if (material == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Material does not exist.");
        }
        var reportedDeliveries = reportedDeliveryService.findAllByFilters(Optional.of(ownMaterialNumber), bpns, bpnl, Optional.empty(), Optional.empty())
            .stream().map(this::convertToDto).collect(Collectors.toList());
        var ownDeliveries = ownDeliveryService.findAllByFilters(Optional.of(ownMaterialNumber), bpns, bpnl, Optional.empty(), Optional.empty())
            .stream().map(this::convertToDto).collect(Collectors.toList());
        return List.of(reportedDeliveries, ownDeliveries).stream().flatMap(List::stream).toList();
    }

    @PostMapping()
    @ResponseBody
    @Operation(summary = "Creates a new delivery", description = "Creates a new delivery.  \n" +
                " **Note:** If the backend should automatically set `lastUpdatedOnDateTime` please set it to null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery was created."),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid request body.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Delivery already exists.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content)
    })
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryDto createDelivery(@RequestBody DeliveryDto deliveryDto) {
        if (!validator.validate(deliveryDto).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (deliveryDto.getOwnMaterialNumber() == null || deliveryDto.getOwnMaterialNumber().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Delivery Information misses material identification.");
        }

        if (deliveryDto.getPartnerBpnl() == null || deliveryDto.getPartnerBpnl().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Delivery Information misses partner identification.");
        }

        if (deliveryDto.getLastUpdatedOnDateTime() == null) {
            deliveryDto.setLastUpdatedOnDateTime(new Date());
        }

        try {
            var dto = convertToDto(ownDeliveryService.create(convertToEntity(deliveryDto)));
            materialService.updateTimestamp(deliveryDto.getOwnMaterialNumber());
            return dto;
        } catch (KeyAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delivery already exists. Use PUT instead.");
        } catch (IllegalArgumentException e) {
            log.info("Delivery is invalid.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery is invalid.");
        }
    }

    @PutMapping()
    @Operation(summary = "Updates a delivery by its UUID", description = "Updates an existing delivery.  \n" +
                " **Note:** If the backend should automatically set `lastUpdatedOnDateTime` please set it to null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Delivery was updated."),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid request body.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Delivery does not exist.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content)
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DeliveryDto updateDelivery(@RequestBody DeliveryDto dto) {
        if (dto.getLastUpdatedOnDateTime() == null) {
            dto.setLastUpdatedOnDateTime(new Date());
        }
        OwnDelivery updatedDelivery = ownDeliveryService.update(convertToEntity(dto));
        if (updatedDelivery == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery does not exist.");
        }
        materialService.updateTimestamp(dto.getOwnMaterialNumber());
        return convertToDto(updatedDelivery);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletes a delivery by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delivery was deleted."),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid request body."),
            @ApiResponse(responseCode = "404", description = "Delivery does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDelivery(@PathVariable UUID id) {
        OwnDelivery delivery = ownDeliveryService.findById(id);
        if (delivery == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery does not exist.");
        }
        ownDeliveryService.delete(id);
        materialService.updateTimestamp(delivery.getMaterial().getOwnMaterialNumber());
    }

    @GetMapping("reported/refresh")
    @ResponseBody
    @Operation(
        summary = "Refreshes all reported deliveries", 
        description = "Refreshes all reported deliveries from the delivery request API."
    )
    public ResponseEntity<List<PartnerDto>> refreshReportedDeliveries(
        @RequestParam @Parameter(description = "encoded in base64") String ownMaterialNumber) {
        ownMaterialNumber = new String(Base64.getDecoder().decode(ownMaterialNumber));
        if (!materialPattern.matcher(ownMaterialNumber).matches()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        Material materialEntity = materialService.findByOwnMaterialNumber(ownMaterialNumber);
        if (materialEntity == null) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }

        Set<Partner> partners = new HashSet<>();
        if (materialEntity.isMaterialFlag()) {
            partners.addAll(mprService.findAllSuppliersForOwnMaterialNumber(ownMaterialNumber));
        } 
        if (materialEntity.isProductFlag()) {
            partners.addAll(mprService.findAllCustomersForOwnMaterialNumber(ownMaterialNumber));
        }
        for (Partner partner : partners) {
            executorService.submit(() ->
            deliveryRequestApiService.doReportedDeliveryRequest(partner, materialEntity));
        }

        return ResponseEntity.ok(partners.stream()
            .map(partner -> modelMapper.map(partner, PartnerDto.class))
            .toList());
    }

    private OwnDelivery convertToEntity(DeliveryDto dto) {
        OwnDelivery entity = modelMapper.map(dto, OwnDelivery.class);

        Material material = materialService.findByOwnMaterialNumber(dto.getOwnMaterialNumber());
        entity.setMaterial(material);

        Partner existingPartner = partnerService.findByBpnl(dto.getPartnerBpnl());

        if (existingPartner == null) {
            throw new IllegalStateException(String.format(
                    "Partner for bpnl %s could not be found",
                    dto.getPartnerBpnl()));
        }
        entity.setPartner(existingPartner);
        return entity;
    }

    private DeliveryDto convertToDto(OwnDelivery entity) {
        DeliveryDto dto = modelMapper.map(entity, DeliveryDto.class);
        dto.setOwnMaterialNumber(entity.getMaterial().getOwnMaterialNumber());
        dto.setPartnerBpnl(entity.getPartner().getBpnl());
        dto.setReported(false);
        return dto;
    }

    private DeliveryDto convertToDto(ReportedDelivery entity) {
        DeliveryDto dto = modelMapper.map(entity, DeliveryDto.class);
        dto.setOwnMaterialNumber(entity.getMaterial().getOwnMaterialNumber());
        dto.setPartnerBpnl(entity.getPartner().getBpnl());
        dto.setReported(true);
        return dto;
    }
}
