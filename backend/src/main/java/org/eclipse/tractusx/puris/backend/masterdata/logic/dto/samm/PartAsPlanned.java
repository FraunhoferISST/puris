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
package org.eclipse.tractusx.puris.backend.masterdata.logic.dto.samm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.tractusx.puris.backend.masterdata.domain.model.Material;

/**
 * Generated class for Part as Planned. A Part as Planned represents an item in
 * the Catena-X Bill of Material(BOM) in As-Planned lifecycle status in a
 * specific version.
 */
public class PartAsPlanned {

	@NotNull
	@Pattern(regexp = "(^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$)|(^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$)")

	private String catenaXId;

	@NotNull
	private PartTypeInformationEntity partTypeInformation;

    @Nullable
	private Set<partSitesInformationAsPlannedEntity> partSitesInformationAsPlanned;

	@JsonCreator
	public PartAsPlanned(@JsonProperty(value = "catenaXId") String catenaXId,
			@JsonProperty(value = "partTypeInformation") PartTypeInformationEntity partTypeInformation,
			@JsonProperty(value = "partSitesInformationAsPlanned") Set<partSitesInformationAsPlannedEntity> partSitesInformationAsPlanned) {
		super(

		);
		this.catenaXId = catenaXId;
		this.partTypeInformation = partTypeInformation;
		this.partSitesInformationAsPlanned = partSitesInformationAsPlanned;
	}

	/**
	 * Returns Catena-X ID
	 *
	 * @return {@link #catenaXId}
	 */
	public String getCatenaXId() {
		return this.catenaXId;
	}

	/**
	 * Returns Part Type Information
	 *
	 * @return {@link #partTypeInformation}
	 */
	public PartTypeInformationEntity getPartTypeInformation() {
		return this.partTypeInformation;
	}

	/**
	 * Returns Part Sites Information as Planned
	 *
	 * @return {@link #partSitesInformationAsPlanned}
	 */
	public Set<partSitesInformationAsPlannedEntity> getPartSitesInformationAsPlanned() {
		return this.partSitesInformationAsPlanned;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final PartAsPlanned that = (PartAsPlanned) o;
		return Objects.equals(catenaXId, that.catenaXId)
				&& Objects.equals(partTypeInformation, that.partTypeInformation)
				&& Objects.equals(partSitesInformationAsPlanned, that.partSitesInformationAsPlanned);
	}

	@Override
	public int hashCode() {
		return Objects.hash(catenaXId, partTypeInformation, partSitesInformationAsPlanned);
	}

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomMaterial extends Material {

        private ClassificationEnumerationCharacteristic classification;

        private String manufacturerPartId;

        private String nameAtManufacturer;
    }
}
