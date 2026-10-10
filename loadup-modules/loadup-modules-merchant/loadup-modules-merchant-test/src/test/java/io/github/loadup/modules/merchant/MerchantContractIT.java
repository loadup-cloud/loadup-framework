/*
 * #%L
 * LoadUp Merchant
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
package io.github.loadup.modules.merchant;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.testcontainers.annotation.*;
import io.github.loadup.modules.contract.app.service.*;
import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.query.ContractResolveQuery;
import io.github.loadup.modules.contract.client.spi.MerchantFactsProvider;
import io.github.loadup.modules.merchant.app.service.MerchantService;
import io.github.loadup.modules.merchant.client.api.MerchantLookup;
import io.github.loadup.modules.merchant.client.command.*;
import io.github.loadup.modules.merchant.client.query.MerchantQuery;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MerchantContractIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
class MerchantContractIT {
    private final MerchantService merchants;
    private final MerchantLookup lookup;
    private final MerchantFactsProvider facts;
    private final CatalogService catalog;
    private final MerchantContractService contracts;
    private final ContractResolveService runtime;

    @Autowired
    MerchantContractIT(
            MerchantService merchants,
            MerchantLookup lookup,
            MerchantFactsProvider facts,
            CatalogService catalog,
            MerchantContractService contracts,
            ContractResolveService runtime) {
        this.merchants = merchants;
        this.lookup = lookup;
        this.facts = facts;
        this.catalog = catalog;
        this.contracts = contracts;
        this.runtime = runtime;
    }

    @Test
    void merchantCrudEnforcesTenantUniqueCodeAndOptimisticVersion() {
        String tenant = UUID.randomUUID().toString();
        String id = TenantUtil.callWithTenant(tenant, () -> {
            var created = merchants.create(command(), "operator");
            assertThatThrownBy(() -> merchants.create(command(), "operator")).isInstanceOf(CommonException.class);
            var updated = merchants.update(
                    new MerchantUpdateCommand(
                            created.id(),
                            created.rowVersion(),
                            "M001",
                            "Updated",
                            null,
                            "ENTERPRISE",
                            "SERVICES",
                            "CN",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null),
                    "operator");
            assertThat(updated.contactPhone()).isEqualTo("13800138000");
            assertThat(facts.load(tenant, created.id())
                            .facts()
                            .get("merchant.industry")
                            .value())
                    .isEqualTo("SERVICES");
            assertThatThrownBy(() -> merchants.changeStatus(
                            new MerchantStatusCommand(created.id(), created.rowVersion(), "INACTIVE"), "operator"))
                    .isInstanceOf(CommonException.class);
            assertThat(merchants
                            .page(new MerchantQuery(null, null, null, 1, 20))
                            .total())
                    .isEqualTo(1);
            return created.id();
        });
        String other = UUID.randomUUID().toString();
        TenantUtil.runWithTenant(other, () -> {
            assertThat(lookup.find(other, id)).isEmpty();
            assertThatThrownBy(() -> lookup.find(tenant, id)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> merchants.detail(id)).isInstanceOf(CommonException.class);
            assertThat(merchants.create(command(), "operator").id()).isNotEqualTo(id);
        });
    }

    @Test
    void contractUsesPersistedIndustryAndDisabledMerchantIsRejected() {
        String tenant = UUID.randomUUID().toString();
        TenantUtil.runWithTenant(tenant, () -> {
            var merchant = merchants.create(command(), "operator");
            String plan = plan();
            var sign = new ContractSignCommand(
                    merchant.id(), "store", plan, "request", Set.of("basic.scan"), Map.of(), null, null);
            var signed = contracts.sign(sign, "operator");
            var query = new ContractResolveQuery(merchant.id(), "store", "basic.scan", Map.of());
            assertThat(runtime.resolve(query).allowed()).isTrue();
            var inactive = merchants.changeStatus(
                    new MerchantStatusCommand(merchant.id(), merchant.rowVersion(), "INACTIVE"), "operator");
            assertThatThrownBy(() -> contracts.preview(
                            new ContractSignCommand(
                                    merchant.id(),
                                    "second",
                                    plan,
                                    "second",
                                    Set.of("basic.scan"),
                                    Map.of(),
                                    null,
                                    null),
                            "operator"))
                    .isInstanceOf(CommonException.class);
            assertThatThrownBy(() -> runtime.resolve(query)).isInstanceOf(CommonException.class);
            assertThat(contracts.sign(sign, "operator").id()).isEqualTo(signed.id());
            var active = merchants.changeStatus(
                    new MerchantStatusCommand(merchant.id(), inactive.rowVersion(), "ACTIVE"), "operator");
            assertThat(runtime.resolve(query).allowed()).isTrue();
            merchants.update(
                    new MerchantUpdateCommand(
                            merchant.id(),
                            active.rowVersion(),
                            "M001",
                            "Retail",
                            null,
                            "ENTERPRISE",
                            "SERVICES",
                            "CN",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null),
                    "operator");
            assertThat(runtime.resolve(query).allowed()).isFalse();
            assertThat(contracts.detail(signed.id()).snapshotHash()).isEqualTo(signed.snapshotHash());
        });
    }

    private MerchantCreateCommand command() {
        return new MerchantCreateCommand(
                "M001",
                "Retail",
                null,
                "ENTERPRISE",
                "RETAIL",
                "CN",
                null,
                null,
                "Address",
                "REG001",
                "Contact",
                "13800138000",
                "test@example.com");
    }

    private String publish(String kind, String code, Map<String, Object> definition) {
        var draft = catalog.save(new CatalogSaveCommand(null, null, kind, code, 1, definition), "operator");
        return catalog.publish(new CatalogActionCommand(draft.id(), draft.rowVersion()), "operator")
                .id();
    }

    private String plan() {
        String rule = publish(
                "CONDITION",
                "RETAIL_ONLY",
                Map.of(
                        "mode",
                        "ALL",
                        "clauses",
                        List.of(Map.of(
                                "kind",
                                "COMPARE",
                                "negated",
                                false,
                                "field",
                                "merchant.industry",
                                "operator",
                                "EQ",
                                "valueType",
                                "STRING",
                                "expected",
                                List.of("RETAIL")))));
        String product = publish(
                "PRODUCT",
                "SCAN",
                Map.of(
                        "capabilityCode",
                        "payment.collect",
                        "parameters",
                        List.of(),
                        "conditionId",
                        rule,
                        "dependencies",
                        List.of(),
                        "exclusions",
                        List.of()));
        String bundle = publish(
                "BUNDLE",
                "BASIC",
                Map.of(
                        "items",
                        List.of(Map.of(
                                "itemKey",
                                "scan",
                                "productId",
                                product,
                                "required",
                                true,
                                "defaultSelected",
                                true,
                                "values",
                                Map.of()))));
        return publish(
                "SALES_PLAN",
                "STANDARD",
                Map.of(
                        "bundles",
                        List.of(Map.of("alias", "basic", "bundleId", bundle)),
                        "values",
                        Map.of(),
                        "merchantPolicies",
                        Map.of(),
                        "eligibilityConditionId",
                        rule,
                        "saleStartsAt",
                        Instant.now().minusSeconds(60).toString()));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}
}
