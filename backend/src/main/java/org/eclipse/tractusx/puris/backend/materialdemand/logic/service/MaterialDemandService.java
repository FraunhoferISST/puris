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
package org.eclipse.tractusx.puris.backend.materialdemand.logic.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.common.api.logic.service.VariablesService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.PartnerService;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.Demand;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.DemandRate;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.DemandSeries;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.MaterialDemand;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.repository.MaterialDemandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MaterialDemandService {

    final private Pattern bpnlPattern = Pattern.compile("^BPNL[0-9]{10}[A-Z]{2}$");
    final private Pattern bpnsPattern = Pattern.compile("^BPNS[0-9]{10}[A-Z]{2}$");

    @Autowired
    private MaterialDemandRepository materialDemandRepository;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private VariablesService variablesService;


    /**
     * Update an existing MaterialDemand entity
     *
     * @param materialDemand representing an object existing in the database but including some changes to be stored.
     * @return the updated entity or null if no corresponding object was found in the database or if
     * the constraints are not met.
     */
    public MaterialDemand update(MaterialDemand materialDemand) {
        if (!testConstraints(materialDemand)) {
            return null;
        }
        var searchResult = materialDemandRepository.findById(materialDemand.getKey());
        if (searchResult.isPresent()) {
            return materialDemandRepository.save(materialDemand);
        }
        log.warn("Failed to update \n" + materialDemand);
        log.warn("No such entity was previously saved to database");
        return null;
    }

    /**
     * Creates a MaterialDemand entity by storing it to the database.
     *
     * @param materialDemand representing a new object to be stored in the database.
     * @return the created object or null if this object existed before or does not meet the constraints.
     */
    public MaterialDemand create(MaterialDemand materialDemand) {
        var searchResult = materialDemandRepository.findById(materialDemand.getKey());
        if (searchResult.isPresent()) {
            return null;
        }
        if (testConstraints(materialDemand)) {
            return materialDemandRepository.save(materialDemand);
        }
        return null;
    }

    private boolean testConstraints(MaterialDemand materialDemand) {
        if (materialDemand.getSupplier() == null || !bpnlPattern.matcher(materialDemand.getSupplier()).matches()) {
            log.warn("Failed to create/update " + materialDemand + "\nBad Supplier BPNL");
            return false;
        }
        if (!materialDemand.getSupplier().equals(variablesService.getOwnBpnl()) &&
            partnerService.findByBpnl(materialDemand.getSupplier()) == null) {
            log.warn("Unknown supplier: " + materialDemand.getSupplier());
        }
        if (materialDemand.getCustomer() == null || !bpnlPattern.matcher(materialDemand.getCustomer()).matches()) {
            log.warn("Failed to create/update " + materialDemand + "\nBad Customer BPNL");
            return false;
        }
        if (!materialDemand.getCustomer().equals(variablesService.getOwnBpnl()) &&
            partnerService.findByBpnl(materialDemand.getCustomer()) == null) {
            log.warn("Unknown customer: " + materialDemand.getCustomer());
        }
        if (materialService.findByOwnMaterialNumber(materialDemand.getMaterialNumberCustomer()) == null
            && materialService.findByOwnMaterialNumber(materialDemand.getMaterialNumberSupplier()) == null) {
            log.warn("Unknown Material: " + materialDemand.getMaterialNumberSupplier() + " " + materialDemand.getMaterialNumberCustomer());
        }
        if (materialDemand.getMaterialDemandId() == null) {
            log.warn("Failed to create/update " + materialDemand + "\nMissing MaterialDemandId");
        }
        for (DemandSeries demandSeries : materialDemand.getDemandSeries()) {
            if (demandSeries.getCustomerLocation() == null || !bpnsPattern.matcher(demandSeries.getCustomerLocation()).matches()) {
                log.warn("Failed to create/update " + materialDemand + "\nBad Customer BPNS in demand series");
                return false;
            }
            if (demandSeries.getExpectedSupplierLocation() == null || !bpnsPattern.matcher(demandSeries.getExpectedSupplierLocation()).matches()) {
                log.warn("Failed to create/update " + materialDemand + "\nBad expected supplier BPNS in demand series");
                return false;
            }
            demandSeries.getKey().setForeignKey(materialDemand.getKey());
            for (Demand demand : demandSeries.getDemands()) {
                if (demand.getDemandRate() == DemandRate.week && !demand.getPointInTime().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                    log.warn("Failed to create/update " + materialDemand + "\nWeekly demand not starting on a monday");
                    return false;
                }
            }
            StringBuilder errorMessageHelper = new StringBuilder();
            var findDoublets = demandSeries.getDemands().stream().filter(d -> {
                for (var other : demandSeries.getDemands()) {
                    if (d != other && d.getDemandRate().equals(other.getDemandRate()) && d.getPointInTime().equals(other.getPointInTime())) {
                        if (d.hashCode() < other.hashCode()) {
                            errorMessageHelper.append(" " + d.getDemandRate() + ", " + d.getPointInTime() + " ");
                        }
                        return true;
                    }
                }
                return false;
            }).findAny();
            if (findDoublets.isPresent()) {
                log.warn("Failed to create/update " + materialDemand + "\nFaulty DemandSeries: More than one demand for a date or calendar week: " + errorMessageHelper);
                return false;
            }

            for (var demand : demandSeries.getDemands()) {
                if (demand.isFixedPointQuantityFlag()) {
                    if (demand.getFixedPointConstraint() <= 0) {
                        log.warn("Fixed point quantity for demand " + demand + " not properly set");
                    }
                } else {
                    if (demand.getRangeUpperBoundary() <= 0 || demand.getRangeLowerBoundary() >= demand.getRangeUpperBoundary()) {
                        log.warn("Quantity range for demand " + demand + " not properly set");
                    }
                }
            }
        }
        return true;
    }

    public MaterialDemand findById(UUID uuid, String customerBPNL) {
        return materialDemandRepository.findById(new MaterialDemand.Key(uuid, customerBPNL)).orElse(null);
    }

    public MaterialDemand find(MaterialDemand materialDemand) {
        return findById(materialDemand.getMaterialDemandId(), materialDemand.getCustomer());
    }

    public List<MaterialDemand> findAllByCustomerBPNL(String customerBPNL) {
        return materialDemandRepository.findAllByKey_Customer(customerBPNL);
    }

    public List<MaterialDemand> findAllBySupplierBPNL(String supplierBPNL) {
        return materialDemandRepository.findAllBySupplier(supplierBPNL);
    }

    public List<MaterialDemand> findAllByOwnMaterialNumber(String ownMaterialNumber) {
        var resultList = materialDemandRepository.findAllByMaterialNumberCustomerOrMaterialNumberSupplier
            (ownMaterialNumber, ownMaterialNumber);
        return resultList
            .stream()
            .filter(m ->
                (m.getSupplier().equals(variablesService.getOwnBpnl()) && m.getMaterialNumberSupplier().equals(ownMaterialNumber))
                    || (m.getCustomer().equals(variablesService.getOwnBpnl()) && m.getMaterialNumberCustomer().equals(ownMaterialNumber)))
            .collect(Collectors.toList());
    }

}
