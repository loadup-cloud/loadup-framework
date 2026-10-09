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

import java.util.HashMap;
import java.util.Map;

public final class ConfigurationResolver {
    public ResolvedConfiguration defaults(ProductVersion product) {
        var values = new HashMap<String, TypedValue>();
        var origins = new HashMap<String, ValueOrigin>();
        product.parameters().forEach((key, definition) -> {
            if (definition.defaultValue() != null) {
                values.put(key, definition.defaultValue());
                origins.put(key, new ValueOrigin(ConfigurationLayer.PRODUCT, product.productCode(), product.version()));
            }
        });
        return new ResolvedConfiguration(values, origins);
    }

    public ResolvedConfiguration apply(
            ProductVersion product,
            ResolvedConfiguration base,
            Map<String, TypedValue> overrides,
            ValueOrigin origin,
            Map<String, OverridePolicy> merchantPolicies) {
        if (origin.layer() == ConfigurationLayer.PRODUCT) throw new IllegalArgumentException("Invalid override layer");
        var values = new HashMap<>(base.values());
        var origins = new HashMap<>(base.origins());
        for (var entry : ContractChecks.map(overrides).entrySet()) {
            ParameterDefinition definition = product.parameters().get(entry.getKey());
            if (definition == null) throw new IllegalArgumentException("Unknown parameter: " + entry.getKey());
            if (!definition.editableLayers().contains(origin.layer()))
                throw new IllegalArgumentException("Override forbidden: " + entry.getKey());
            definition.validate(entry.getValue());
            if (origin.layer() == ConfigurationLayer.MERCHANT) {
                OverridePolicy policy = merchantPolicies.get(entry.getKey());
                if (policy == null)
                    throw new IllegalArgumentException("Merchant permission missing: " + entry.getKey());
                policy.validateDefinition(definition);
                policy.validate(definition, entry.getValue());
            }
            values.put(entry.getKey(), entry.getValue());
            origins.put(entry.getKey(), origin);
        }
        return new ResolvedConfiguration(values, origins);
    }

    public void validateComplete(ProductVersion product, ResolvedConfiguration configuration) {
        for (var entry : configuration.values().entrySet()) {
            ParameterDefinition definition = product.parameters().get(entry.getKey());
            if (definition == null) throw new IllegalArgumentException("Unknown resolved parameter");
            definition.validate(entry.getValue());
        }
        product.parameters().forEach((key, definition) -> {
            if (definition.required() && !configuration.values().containsKey(key))
                throw new IllegalArgumentException("Missing required parameter: " + key);
        });
    }
}
