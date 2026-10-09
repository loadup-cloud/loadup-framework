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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class SelectionValidator {
    private SelectionValidator() {}

    static void validate(Map<String, SalesPlanVersion.Item> items, Set<String> selected) {
        if (selected.isEmpty() || !items.keySet().containsAll(selected))
            throw new IllegalArgumentException("Invalid product selection");
        if (items.entrySet().stream().anyMatch(e -> e.getValue().required() && !selected.contains(e.getKey()))) {
            throw new IllegalArgumentException("Required product omitted");
        }
        Set<String> productCodes = selected.stream()
                .map(key -> items.get(key).product().productCode())
                .collect(Collectors.toSet());
        for (String key : selected) {
            ProductVersion product = items.get(key).product();
            if (!productCodes.containsAll(product.dependencies()))
                throw new IllegalArgumentException("Missing product dependency");
            if (product.exclusions().stream().anyMatch(productCodes::contains))
                throw new IllegalArgumentException("Mutually exclusive products");
        }
    }
}
