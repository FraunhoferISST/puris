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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

/**
 * Generated class {@link FunctionCharacteristic}.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FunctionCharacteristic {
	PRODUCTION("production"), WAREHOUSE("warehouse"), SPARE_PART_WAREHOUSE("spare part warehouse");

	private String value;

	FunctionCharacteristic(String value) {
		this.value = value;
	}

	@JsonCreator
	static FunctionCharacteristic enumDeserializationConstructor(String value) {
		return fromValue(value).orElseThrow(() -> new RuntimeException("Tried to parse value \"" + value
				+ "\", but there is no enum field like that in FunctionCharacteristic"));
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	public static Optional<FunctionCharacteristic> fromValue(String value) {
		return Arrays.stream(FunctionCharacteristic.values()).filter(enumValue -> compareEnumValues(enumValue, value))
				.findAny();
	}

	private static boolean compareEnumValues(FunctionCharacteristic enumValue, String value) {
		return enumValue.getValue().equals(value);
	}

}
