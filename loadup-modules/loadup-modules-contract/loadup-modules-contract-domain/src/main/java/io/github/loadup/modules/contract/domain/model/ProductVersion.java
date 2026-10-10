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
import java.util.Map;
import java.util.Set;

/** Published product contents. Storage adapters must enforce tenant ownership of all references. */
public record ProductVersion(
        String productCode,
        int version,
        String capabilityCode,
        Map<String, ParameterDefinition> parameters,
        Condition usageCondition,
        Set<String> dependencies,
        Set<String> exclusions) {
    public ProductVersion {
        productCode = ContractChecks.code(productCode);
        version = ContractChecks.version(version);
        capabilityCode = ContractChecks.code(capabilityCode);
        parameters = ContractChecks.map(parameters);
        for (var entry : parameters.entrySet()) {
            if (!entry.getKey().equals(entry.getValue().key()))
                throw new IllegalArgumentException("Parameter key mismatch");
        }
        dependencies = Set.copyOf(dependencies);
        exclusions = Set.copyOf(exclusions);
        dependencies.forEach(ContractChecks::code);
        exclusions.forEach(ContractChecks::code);
        if (dependencies.contains(productCode)
                || exclusions.contains(productCode)
                || dependencies.stream().anyMatch(exclusions::contains))
            throw new IllegalArgumentException("Conflicting product relationships");
        new ConditionEvaluator().validateConfigurationReferences(usageCondition, parameters.keySet());
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
