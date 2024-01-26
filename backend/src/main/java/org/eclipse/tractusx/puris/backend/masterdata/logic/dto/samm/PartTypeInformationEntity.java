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
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Generated class for Part Type Information Entity. Encapsulation for data
 * related to the part type
 */

public class PartTypeInformationEntity {

	@NotNull
	private String manufacturerPartId;

	@NotNull
	private String nameAtManufacturer;

	@NotNull
	private ClassificationEnumerationCharacteristic classification;

	@JsonCreator
	public PartTypeInformationEntity(@JsonProperty(value = "manufacturerPartId") String manufacturerPartId,
			@JsonProperty(value = "nameAtManufacturer") String nameAtManufacturer,
			@JsonProperty(value = "classification") ClassificationEnumerationCharacteristic classification) {
		super(

		);
		this.manufacturerPartId = manufacturerPartId;
		this.nameAtManufacturer = nameAtManufacturer;
		this.classification = classification;
	}

	/**
	 * Returns Manufacturer Part ID
	 *
	 * @return {@link #manufacturerPartId}
	 */
	public String getManufacturerPartId() {
		return this.manufacturerPartId;
	}

	/**
	 * Returns Name at manufacturer
	 *
	 * @return {@link #nameAtManufacturer}
	 */
	public String getNameAtManufacturer() {
		return this.nameAtManufacturer;
	}

	/**
	 * Returns Classification
	 *
	 * @return {@link #classification}
	 */
	public ClassificationEnumerationCharacteristic getClassification() {
		return this.classification;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final PartTypeInformationEntity that = (PartTypeInformationEntity) o;
		return Objects.equals(manufacturerPartId, that.manufacturerPartId)
				&& Objects.equals(nameAtManufacturer, that.nameAtManufacturer)
				&& Objects.equals(classification, that.classification);
	}

	@Override
	public int hashCode() {
		return Objects.hash(manufacturerPartId, nameAtManufacturer, classification);
	}
}
