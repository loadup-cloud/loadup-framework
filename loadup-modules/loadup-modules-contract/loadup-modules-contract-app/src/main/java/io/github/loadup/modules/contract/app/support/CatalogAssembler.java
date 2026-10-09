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
package io.github.loadup.modules.contract.app.support;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.app.converter.ContractConverter;
import io.github.loadup.modules.contract.client.dto.CatalogDefinitions;
import io.github.loadup.modules.contract.client.dto.ContractError;
import io.github.loadup.modules.contract.domain.gateway.CatalogGateway;
import io.github.loadup.modules.contract.domain.model.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** Interprets validated wire definitions; references always resolve in the same tenant. */
@Component
public class CatalogAssembler {
    private final CatalogGateway gateway;
    private final ContractCodec codec;
    private final ContractConverter converter;

    public CatalogAssembler(CatalogGateway gateway, ContractCodec codec, ContractConverter converter) {
        this.gateway = gateway;
        this.codec = codec;
        this.converter = converter;
    }

    public void validate(CatalogEntry entry, boolean publishing) {
        switch (entry.kind()) {
            case PRODUCT -> product(entry, publishing);
            case CONDITION -> condition(entry);
            case BUNDLE -> bundle(entry, publishing);
            case SALES_PLAN -> plan(entry, publishing);
        }
    }

    private CatalogEntry reference(String tenant, String id, CatalogKind kind, boolean publishing) {
        CatalogEntry entry = (publishing ? gateway.lock(tenant, id) : gateway.find(tenant, id))
                .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        if (entry.kind() != kind
                || entry.status() == CatalogStatus.DRAFT
                || publishing && entry.status() != CatalogStatus.PUBLISHED)
            throw new CommonException(
                    ContractError.INVALID_STATE, "Reference must be a published version of the expected kind");
        return entry;
    }

    private Condition condition(String tenant, String id, boolean publishing) {
        return id == null || id.isBlank()
                ? Condition.always()
                : condition(reference(tenant, id, CatalogKind.CONDITION, publishing));
    }

    public Condition condition(CatalogEntry entry) {
        var definition = codec.read(entry.content(), CatalogDefinitions.ConditionDefinition.class);
        if (definition.clauses() == null || definition.clauses().size() > 100)
            throw new IllegalArgumentException("Invalid condition clauses");
        List<Condition> clauses = new ArrayList<>();
        for (var clause : definition.clauses()) {
            if (!clause.field().startsWith("merchant.") && !clause.field().startsWith("transaction."))
                throw new IllegalArgumentException("Unsupported fact namespace");
            Condition node;
            if ("EXISTS".equals(clause.kind())) node = new Condition.Exists(clause.field());
            else if ("COMPARE".equals(clause.kind())) {
                var expected = clause.expected() == null
                        ? List.<TypedValue>of()
                        : clause.expected().stream()
                                .map(v -> ContractMappingSupport.value(clause.valueType(), v))
                                .toList();
                String key = clause.configurationKey() == null
                                || clause.configurationKey().isBlank()
                        ? null
                        : clause.configurationKey();
                node = new Condition.Compare(
                        clause.field(), ComparisonOperator.valueOf(clause.operator()), expected, key);
            } else throw new IllegalArgumentException("Unknown clause kind");
            clauses.add(clause.negated() ? new Condition.Not(node) : node);
        }
        Condition condition =
                switch (definition.mode()) {
                    case "ALL" -> new Condition.All(clauses);
                    case "ANY" -> new Condition.Any(clauses);
                    default -> throw new IllegalArgumentException("Unknown condition mode");
                };
        new ConditionEvaluator().validate(condition);
        return condition;
    }

    public ProductVersion product(CatalogEntry entry, boolean publishing) {
        var d = codec.read(entry.content(), CatalogDefinitions.Product.class);
        if (d.parameters() == null || d.parameters().size() > 256)
            throw new IllegalArgumentException("Invalid parameter count");
        var parameters = new HashMap<String, ParameterDefinition>();
        for (var parameter : d.parameters()) {
            ParameterDefinition mapped = converter.toParameter(parameter);
            if (parameters.putIfAbsent(mapped.key(), mapped) != null)
                throw new IllegalArgumentException("Duplicate parameter");
        }
        return new ProductVersion(
                entry.code(),
                entry.version(),
                d.capabilityCode(),
                parameters,
                condition(entry.tenantId(), d.conditionId(), publishing),
                set(d.dependencies()),
                set(d.exclusions()));
    }

    public BundleVersion bundle(CatalogEntry entry, boolean publishing) {
        var d = codec.read(entry.content(), CatalogDefinitions.Bundle.class);
        if (d.items() == null || d.items().isEmpty() || d.items().size() > 128)
            throw new IllegalArgumentException("Invalid bundle items");
        List<BundleVersion.Item> items = new ArrayList<>();
        for (var item : d.items()) {
            ProductVersion product =
                    product(reference(entry.tenantId(), item.productId(), CatalogKind.PRODUCT, publishing), publishing);
            Map<String, TypedValue> values = typedValues(product, item.values());
            new ConfigurationResolver()
                    .apply(
                            product,
                            new ConfigurationResolver().defaults(product),
                            values,
                            new ValueOrigin(ConfigurationLayer.BUNDLE, entry.code(), entry.version()),
                            Map.of());
            items.add(new BundleVersion.Item(
                    item.itemKey(),
                    product,
                    item.required(),
                    item.defaultSelected(),
                    values,
                    condition(entry.tenantId(), item.conditionId(), publishing)));
        }
        return new BundleVersion(entry.code(), entry.version(), items);
    }

    public SalesPlanVersion plan(CatalogEntry entry, boolean publishing) {
        var d = codec.read(entry.content(), CatalogDefinitions.Plan.class);
        if (d.bundles() == null || d.bundles().isEmpty() || d.bundles().size() > 128)
            throw new IllegalArgumentException("Invalid plan bundles");
        List<SalesPlanDraft.BundleSelection> bundles = new ArrayList<>();
        Map<String, ProductVersion> products = new HashMap<>();
        for (var selected : d.bundles()) {
            BundleVersion bundle = bundle(
                    reference(entry.tenantId(), selected.bundleId(), CatalogKind.BUNDLE, publishing), publishing);
            bundles.add(new SalesPlanDraft.BundleSelection(selected.alias(), bundle));
            for (var item : bundle.items()) {
                if (products.putIfAbsent(selected.alias() + "." + item.itemKey(), item.product()) != null)
                    throw new IllegalArgumentException("Duplicate plan item");
            }
        }
        Map<String, Map<String, TypedValue>> values = new HashMap<>();
        if (d.values() != null)
            d.values()
                    .forEach(
                            (key, overrides) -> values.put(key, typedValues(requireProduct(products, key), overrides)));
        Map<String, Map<String, OverridePolicy>> policies = new HashMap<>();
        if (d.merchantPolicies() != null)
            d.merchantPolicies().forEach((key, fields) -> {
                ProductVersion product = requireProduct(products, key);
                Map<String, OverridePolicy> mapped = new HashMap<>();
                fields.forEach((field, policy) -> {
                    ParameterDefinition parameter = product.parameters().get(field);
                    if (parameter == null) throw new IllegalArgumentException("Unknown policy field");
                    mapped.put(
                            field,
                            new OverridePolicy(
                                    ContractMappingSupport.decimal(policy.minimum()),
                                    ContractMappingSupport.decimal(policy.maximum()),
                                    ContractMappingSupport.values(
                                            parameter.type().name(), policy.allowedValues())));
                });
                policies.put(key, mapped);
            });
        Condition eligibility = condition(entry.tenantId(), d.eligibilityConditionId(), publishing);
        merchantOnly(eligibility);
        return new SalesPlanCompiler()
                .publish(new SalesPlanDraft(
                        entry.code(),
                        entry.version(),
                        bundles,
                        values,
                        policies,
                        eligibility,
                        condition(entry.tenantId(), d.usageConditionId(), publishing),
                        d.saleStartsAt(),
                        d.saleEndsAt()));
    }

    private void merchantOnly(Condition condition) {
        switch (condition) {
            case Condition.All all -> all.children().forEach(this::merchantOnly);
            case Condition.Any any -> any.children().forEach(this::merchantOnly);
            case Condition.Not not -> merchantOnly(not.child());
            case Condition.Exists exists -> {
                if (!exists.field().startsWith("merchant."))
                    throw new IllegalArgumentException("Eligibility requires merchant facts");
            }
            case Condition.Compare compare -> {
                if (!compare.field().startsWith("merchant."))
                    throw new IllegalArgumentException("Eligibility requires merchant facts");
            }
        }
    }

    private static ProductVersion requireProduct(Map<String, ProductVersion> products, String key) {
        ProductVersion product = products.get(key);
        if (product == null) throw new IllegalArgumentException("Unknown plan item");
        return product;
    }

    public Map<String, TypedValue> typedValues(ProductVersion product, Map<String, String> input) {
        Map<String, TypedValue> result = new HashMap<>();
        if (input == null) return Map.of();
        input.forEach((field, value) -> {
            ParameterDefinition parameter = product.parameters().get(field);
            if (parameter == null || value == null) throw new IllegalArgumentException("Unknown or null parameter");
            result.put(field, ContractMappingSupport.value(parameter.type().name(), value));
        });
        return Map.copyOf(result);
    }

    public Map<String, Map<String, TypedValue>> merchantValues(
            SalesPlanVersion plan, Map<String, Map<String, String>> input) {
        if (input == null) throw new IllegalArgumentException("Values required");
        var mapped = new HashMap<String, Map<String, TypedValue>>();
        input.forEach((key, value) -> {
            var item = plan.items().get(key);
            if (item == null) throw new IllegalArgumentException("Unknown signed item");
            mapped.put(key, typedValues(item.product(), value));
        });
        return mapped;
    }

    private static <T> Set<T> set(Set<T> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}
