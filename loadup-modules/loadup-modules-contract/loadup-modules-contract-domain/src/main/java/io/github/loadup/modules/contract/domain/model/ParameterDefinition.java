/*
 * #%L
 * LoadUp Contract
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.contract.domain.model;

import io.github.loadup.commons.json.ToStringAsJson;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public record ParameterDefinition(
        String key,
        ValueType type,
        boolean required,
        TypedValue defaultValue,
        BigDecimal minimum,
        BigDecimal maximum,
        Set<TypedValue> allowedValues,
        Set<ConfigurationLayer> editableLayers) {
    public ParameterDefinition {
        key = ContractChecks.code(key);
        Objects.requireNonNull(type, "parameter type");
        allowedValues = Set.copyOf(allowedValues);
        editableLayers = Set.copyOf(editableLayers);
        if (editableLayers.contains(ConfigurationLayer.PRODUCT)) {
            throw new IllegalArgumentException("Product defaults are not an override layer");
        }
        if (allowedValues.size() > 100) throw new IllegalArgumentException("Too many allowed values");
        if (allowedValues.stream().anyMatch(v -> v.type() != type)) {
            throw new IllegalArgumentException("Allowed value type mismatch");
        }
        checkBounds(type, minimum, maximum);
        for (TypedValue value : allowedValues) checkValue(type, value, minimum, maximum, Set.of());
        if (defaultValue != null) checkValue(type, defaultValue, minimum, maximum, allowedValues);
    }

    public void validate(TypedValue value) {
        checkValue(type, value, minimum, maximum, allowedValues);
    }

    static void checkBounds(ValueType type, BigDecimal minimum, BigDecimal maximum) {
        if ((minimum != null || maximum != null) && type != ValueType.INTEGER && type != ValueType.DECIMAL) {
            throw new IllegalArgumentException("Bounds require numeric parameters");
        }
        if (type == ValueType.INTEGER
                && minimum != null
                && maximum != null
                && minimum.setScale(0, java.math.RoundingMode.CEILING)
                                .compareTo(maximum.setScale(0, java.math.RoundingMode.FLOOR))
                        > 0) {
            throw new IllegalArgumentException("Integer range contains no value");
        }
        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Inverted bounds");
        }
    }

    static void checkValue(
            ValueType type, TypedValue value, BigDecimal minimum, BigDecimal maximum, Set<TypedValue> allowed) {
        Objects.requireNonNull(value, "parameter value");
        if (value.type() != type) throw new IllegalArgumentException("Parameter type mismatch");
        if (!allowed.isEmpty() && !allowed.contains(value)) throw new IllegalArgumentException("Value not allowed");
        if (minimum != null && value.number().compareTo(minimum) < 0)
            throw new IllegalArgumentException("Below minimum");
        if (maximum != null && value.number().compareTo(maximum) > 0)
            throw new IllegalArgumentException("Above maximum");
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
