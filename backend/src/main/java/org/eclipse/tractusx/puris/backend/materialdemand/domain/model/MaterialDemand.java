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
package org.eclipse.tractusx.puris.backend.materialdemand.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the content a MaterialDemand message that is sent or
 * that was received by a partner.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MaterialDemand {

    @EmbeddedId
    private Key key = new Key();

    @OneToMany(cascade = CascadeType.ALL)
    private List<DemandSeries> demandSeries = new ArrayList<>();
    // BPNL
    private String supplier;
    // example "Spark Plug"
    private String materialDescriptionCustomer;

    private UUID materialNumberCatenaX;

    private String materialNumberCustomer;

    private String materialNumberSupplier;

    private UnitOfMeasure unitOfMeasure;

    private LocalDateTime changedAt;

    public String getCustomer() {
        return key.getCustomer();
    }

    public UUID getMaterialDemandId() {
        return key.getMaterialDemandId();
    }


    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Key implements Serializable {
        private UUID materialDemandId;
        // BPNL
        private String customer;

    }


}
