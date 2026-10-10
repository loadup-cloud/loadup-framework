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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BundleVersion(String bundleCode, int version, List<Item> items) {
    public BundleVersion {
        bundleCode = ContractChecks.code(bundleCode);
        version = ContractChecks.version(version);
        items = List.copyOf(items);
        if (items.isEmpty() || items.size() > 128) throw new IllegalArgumentException("Invalid bundle size");
        var keys = new HashSet<String>();
        for (Item item : items)
            if (!keys.add(item.itemKey())) throw new IllegalArgumentException("Duplicate bundle item");
    }

    public record Item(
            String itemKey,
            ProductVersion product,
            boolean required,
            boolean defaultSelected,
            Map<String, TypedValue> values,
            Condition usageCondition) {
        public Item {
            itemKey = ContractChecks.code(itemKey);
            Objects.requireNonNull(product, "product");
            if (required && !defaultSelected)
                throw new IllegalArgumentException("Required item must be selected by default");
            values = ContractChecks.map(values);
            new ConditionEvaluator().validate(usageCondition);
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
