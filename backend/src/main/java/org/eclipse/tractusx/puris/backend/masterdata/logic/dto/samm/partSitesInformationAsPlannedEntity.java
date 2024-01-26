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
import java.util.Objects;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Generated class for Part Sites Information as Planned Entity. Describes the
 * ID, function and validity date of a site for the associated part in the
 * AsPlanned context.
 */

public class partSitesInformationAsPlannedEntity {

	@NotNull
	private String catenaXsiteId;

	@NotNull
	private FunctionCharacteristic function;
    @Nullable
	private XMLGregorianCalendar functionValidFrom;
    @Nullable
	private XMLGregorianCalendar functionValidUntil;

	@JsonCreator
	public partSitesInformationAsPlannedEntity(@JsonProperty(value = "catenaXsiteId") String catenaXsiteId,
			@JsonProperty(value = "function") FunctionCharacteristic function,
			@JsonProperty(value = "functionValidFrom") XMLGregorianCalendar functionValidFrom,
			@JsonProperty(value = "functionValidUntil") XMLGregorianCalendar functionValidUntil) {
		super(

		);
		this.catenaXsiteId = catenaXsiteId;
		this.function = function;
		this.functionValidFrom = functionValidFrom;
		this.functionValidUntil = functionValidUntil;
	}

	/**
	 * Returns Catena-X site identifier
	 *
	 * @return {@link #catenaXsiteId}
	 */
	public String getCatenaXsiteId() {
		return this.catenaXsiteId;
	}

	/**
	 * Returns Function
	 *
	 * @return {@link #function}
	 */
	public FunctionCharacteristic getFunction() {
		return this.function;
	}

	/**
	 * Returns Function valid from
	 *
	 * @return {@link #functionValidFrom}
	 */
	public XMLGregorianCalendar getFunctionValidFrom() {
		return this.functionValidFrom;
	}

	/**
	 * Returns Function valid until
	 *
	 * @return {@link #functionValidUntil}
	 */
	public XMLGregorianCalendar getFunctionValidUntil() {
		return this.functionValidUntil;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final partSitesInformationAsPlannedEntity that = (partSitesInformationAsPlannedEntity) o;
		return Objects.equals(catenaXsiteId, that.catenaXsiteId) && Objects.equals(function, that.function)
				&& Objects.equals(functionValidFrom, that.functionValidFrom)
				&& Objects.equals(functionValidUntil, that.functionValidUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(catenaXsiteId, function, functionValidFrom, functionValidUntil);
	}
}
