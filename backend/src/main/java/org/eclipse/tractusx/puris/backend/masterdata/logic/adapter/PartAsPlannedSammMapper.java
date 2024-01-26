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

package org.eclipse.tractusx.puris.backend.masterdata.logic.adapter;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Material;
import org.eclipse.tractusx.puris.backend.masterdata.logic.dto.samm.PartAsPlanned;
import org.eclipse.tractusx.puris.backend.masterdata.logic.dto.samm.PartAsPlanned.CustomMaterial;
import org.eclipse.tractusx.puris.backend.masterdata.logic.dto.samm.PartTypeInformationEntity;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialPartnerRelationService;
import org.eclipse.tractusx.puris.backend.masterdata.logic.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PartAsPlannedSammMapper {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private MaterialPartnerRelationService mprService;

    public PartAsPlanned materialToPartAsPlanned(CustomMaterial material) {
        var partTypeInformation = new PartTypeInformationEntity(material.getManufacturerPartId(),
            material.getNameAtManufacturer(), material.getClassification());
        return new PartAsPlanned(material.getMaterialNumberCx(), partTypeInformation, Set.of());
    }

    public CustomMaterial partAsPlannedToMaterial(PartAsPlanned partAsPlanned) {
        Material material = null;

        var cxNumber = partAsPlanned.getCatenaXId();
        if (cxNumber != null) {
            material = materialService.findByMaterialNumberCx(cxNumber);
        }

        var manufacturerPartId = partAsPlanned.getPartTypeInformation().getManufacturerPartId();
        if (material == null && manufacturerPartId != null) {
            var materials = mprService.findAllBySupplierPartnerMaterialNumber(manufacturerPartId);

            if (!materials.isEmpty()) {
                material = materials.get(0);
                if (materials.size() > 1) {
                    log.warn("Supplier Partner Materials " + manufacturerPartId
                        + " is ambiguous, arbitrarily choosing " + material.getOwnMaterialNumber());
                }
            }
        }

        if(material != null){
            return null;
        }else{
            // TODO: Refactor by removing CustomMaterial, rather enhance Material.
            var customMaterial = new CustomMaterial();
            customMaterial.setMaterialFlag(material.isMaterialFlag());
            customMaterial.setProductFlag(material.isProductFlag());
            customMaterial.setOwnMaterialNumber(material.getOwnMaterialNumber());
            customMaterial.setMaterialNumberCx(material.getMaterialNumberCx());
            customMaterial.setName(material.getName());
            customMaterial.setMaterialPartnerRelations(material.getMaterialPartnerRelations());

            var partTypeInformation = partAsPlanned.getPartTypeInformation();
            customMaterial.setClassification(partTypeInformation.getClassification());
            customMaterial.setManufacturerPartId(partTypeInformation.getManufacturerPartId());
            customMaterial.setNameAtManufacturer(partTypeInformation.getNameAtManufacturer());

            return customMaterial;
        }
    }

}
