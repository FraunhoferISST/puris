/*
 * Copyright (c) 2023 Volkswagen AG
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

package org.eclipse.tractusx.puris.backend.stock.domain.repository;

import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Material;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Partner;
import org.eclipse.tractusx.puris.backend.stock.domain.model.ProductItemStock;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductItemStockRepository extends ItemStockRepository<ProductItemStock> {
    List<ProductItemStock> findByPartnerAndMaterial(Partner partner, Material material);

    List<ProductItemStock> findByPartner(Partner partner);

    List<ProductItemStock> findByMaterial(Material material);

    List<ProductItemStock> findByMaterial_OwnMaterialNumber(String ownMaterialNumber);

    List<ProductItemStock> findByPartner_Bpnl(String partnerBpnl);

    List<ProductItemStock> findByPartner_BpnlAndMaterial_OwnMaterialNumber(String partnerBpnl, String ownMaterialNumber);

    @Override
    default List<ProductItemStock> getForPartnerAndMaterial(Partner partner, Material material) {
        return findByPartnerAndMaterial(partner, material);
    }

    @Override
    default List<ProductItemStock> getForPartner(Partner partner) {
        return findByPartner(partner);
    }

    @Override
    default List<ProductItemStock> getForMaterial(Material material) {
        return findByMaterial(material);
    }

    @Override
    default List<ProductItemStock> getForOwnMatNbr(String ownMaterialNumber) {
        return findByMaterial_OwnMaterialNumber(ownMaterialNumber);
    }

    @Override
    default List<ProductItemStock> getForPartnerBpnl(String partnerBpnl) {
        return findByPartner_Bpnl(partnerBpnl);
    }

    @Override
    default List<ProductItemStock> getForPartnerBpnlAndOwnMatNbr(String partnerBpnl, String ownMaterialNumber) {
        return findByPartner_BpnlAndMaterial_OwnMaterialNumber(partnerBpnl, ownMaterialNumber);
    }
}
