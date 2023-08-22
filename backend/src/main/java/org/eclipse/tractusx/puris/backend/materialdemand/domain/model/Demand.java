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

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class Demand {

    private LocalDate pointInTime;

    private DemandRate demandRate;

    private boolean fixedPointQuantityFlag;

    double fixedPointConstraint;

    double rangeLowerBoundary;
    double rangeUpperBoundary;

    @Override
    public String toString() {
        String output = "Demand{" +
            "pointInTime=" + pointInTime +
            ", demandRate=" + demandRate;
        if (fixedPointQuantityFlag) {
            output += ", fixedPointConstraint=" + fixedPointConstraint;
        } else {
            output += ", rangeLowerBoundary=" + rangeLowerBoundary +
                ", rangeUpperBoundary=" + rangeUpperBoundary;
        }
        return output + '}';
    }
}
