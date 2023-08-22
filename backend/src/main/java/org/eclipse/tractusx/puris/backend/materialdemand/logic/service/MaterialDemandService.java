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
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.PartnerService;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.Demand;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.DemandRate;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.DemandSeries;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.MaterialDemand;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.repository.MaterialDemandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
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
    private VariablesService variablesService;


    public MaterialDemand create(MaterialDemand materialDemand) {
        if (materialDemand.getSupplier() == null || !bpnlPattern.matcher(materialDemand.getSupplier()).matches()) {
            log.warn("Failed to create " + materialDemand + "\nBad Supplier BPNL");
            return null;
        }
        if (!materialDemand.getSupplier().equals(variablesService.getOwnBpnl()) &&
            partnerService.findByBpnl(materialDemand.getSupplier()) == null) {
            log.warn("Unknown supplier: " + materialDemand.getSupplier());
        }
        if (materialDemand.getCustomer() == null || !bpnlPattern.matcher(materialDemand.getCustomer()).matches()) {
            log.warn("Failed to create " + materialDemand + "\nBad Customer BPNL");
            return null;
        }
        if (!materialDemand.getCustomer().equals(variablesService.getOwnBpnl()) &&
            partnerService.findByBpnl(materialDemand.getCustomer()) == null) {
            log.warn("Unknown customer: " + materialDemand.getCustomer());
        }
        if (materialDemand.getMaterialDemandId() == null) {
            log.warn("Failed to create " + materialDemand + "\nMissing MaterialDemandId");
        }
        for (DemandSeries demandSeries : materialDemand.getDemandSeries()) {
            if (demandSeries.getCustomerLocation() == null || !bpnsPattern.matcher(demandSeries.getCustomerLocation()).matches()) {
                log.warn("Failed to create " + materialDemand + "\nBad Customer BPNS in demand series");
                return null;
            }
            if (demandSeries.getExpectedSupplierLocation() == null || !bpnsPattern.matcher(demandSeries.getExpectedSupplierLocation()).matches()) {
                log.warn("Failed to create " + materialDemand + "\nBad expected supplier BPNS in demand series");
                return null;
            }
            demandSeries.getKey().setForeignKey(materialDemand.getKey());
            for (Demand demand : demandSeries.getDemands()) {
                if (demand.getDemandRate() == DemandRate.week && !demand.getPointInTime().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                    log.warn("Failed to create " + materialDemand + "\nWeekly demand not starting on a monday");
                    return null;
                }
            }
            StringBuilder errorMessageHelper = new StringBuilder();
            var findDoublets = demandSeries.getDemands().stream().filter(d -> {
                for (var other : demandSeries.getDemands()) {
                    if (d != other && d.getDemandRate().equals(other.getDemandRate()) && d.getPointInTime().equals(other.getPointInTime())) {
                        if(d.hashCode() < other.hashCode()) {
                            errorMessageHelper.append(" " + d.getDemandRate() + ", " + d.getPointInTime() + " ");
                        }
                        return true;
                    }
                }
                return false;
            }).findAny();
            if(findDoublets.isPresent()) {
                log.warn("Failed to create " + materialDemand + "\nFaulty DemandSeries: More than one demand for a date or calendar week: " + errorMessageHelper);
                return null;
            }
        }

        return materialDemandRepository.save(materialDemand);
    }

    public MaterialDemand findById(UUID uuid, String customerBPNL) {
        return materialDemandRepository.findById(new MaterialDemand.Key(uuid, customerBPNL)).orElse(null);
    }

}
