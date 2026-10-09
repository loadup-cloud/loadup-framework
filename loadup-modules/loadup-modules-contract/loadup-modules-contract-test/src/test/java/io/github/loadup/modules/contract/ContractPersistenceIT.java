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
package io.github.loadup.modules.contract;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.modules.contract.app.service.*;
import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.dto.*;
import io.github.loadup.modules.contract.client.query.ContractResolveQuery;
import io.github.loadup.modules.contract.client.spi.MerchantFactsProvider;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ContractPersistenceIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
class ContractPersistenceIT {
    private final CatalogService catalog;
    private final MerchantContractService contracts;
    private final ContractResolveService runtime;

    @Autowired
    ContractPersistenceIT(CatalogService catalog, MerchantContractService contracts, ContractResolveService runtime) {
        this.catalog = catalog;
        this.contracts = contracts;
        this.runtime = runtime;
    }

    @Test
    void publishedVersionsCannotBeEditedAndReferencesAreTenantScoped() {
        String tenant = UUID.randomUUID().toString();
        CatalogVersionDTO product = TenantUtil.callWithTenant(tenant, () -> product());
        TenantUtil.runWithTenant(
                tenant,
                () -> assertThatThrownBy(() -> catalog.save(
                                new CatalogSaveCommand(
                                        product.id(),
                                        product.rowVersion(),
                                        "PRODUCT",
                                        product.code(),
                                        1,
                                        product.definition()),
                                "operator"))
                        .isInstanceOf(CommonException.class));
        TenantUtil.runWithTenant(
                "other",
                () -> assertThatThrownBy(() -> catalog.save(
                                new CatalogSaveCommand(
                                        null, null, "BUNDLE", "BASIC", 1, bundleDefinition(product.id())),
                                "operator"))
                        .isInstanceOf(CommonException.class));
    }

    @Test
    void staleDraftAndStateGenerationsAreRejected() {
        TenantUtil.runWithTenant(UUID.randomUUID().toString(), () -> {
            var draft = catalog.save(
                    new CatalogSaveCommand(null, null, "PRODUCT", "SCAN", 1, productDefinition()), "operator");
            var updated = catalog.save(
                    new CatalogSaveCommand(draft.id(), draft.rowVersion(), "PRODUCT", "SCAN", 1, productDefinition()),
                    "operator");
            assertThatThrownBy(
                            () -> catalog.publish(new CatalogActionCommand(draft.id(), draft.rowVersion()), "operator"))
                    .isInstanceOf(CommonException.class);
            assertThat(catalog.publish(new CatalogActionCommand(updated.id(), updated.rowVersion()), "operator")
                            .status())
                    .isEqualTo("PUBLISHED");
        });
    }

    @Test
    void signReplaysDurablyAndSuspensionImmediatelyDeniesRuntime() {
        TenantUtil.runWithTenant(UUID.randomUUID().toString(), () -> {
            String plan = plan();
            var command = command(plan, "request");
            var signed = contracts.sign(command, "operator");
            assertThat(contracts.sign(command, "operator").id()).isEqualTo(signed.id());
            assertThatThrownBy(() -> contracts.sign(
                            new ContractSignCommand(
                                    "merchant", "other", plan, "request", Set.of("basic.scan"), Map.of(), null, null),
                            "operator"))
                    .isInstanceOf(CommonException.class);
            assertThat(runtime.resolve(new ContractResolveQuery("merchant", "store", "basic.scan", Map.of()))
                            .allowed())
                    .isTrue();
            var suspended = contracts.changeStatus(
                    new ContractStatusCommand(signed.id(), signed.generation(), "SUSPENDED"), "operator");
            assertThat(runtime.resolve(new ContractResolveQuery("merchant", "store", "basic.scan", Map.of()))
                            .reason())
                    .isEqualTo("SUSPENDED");
            assertThatThrownBy(() -> contracts.changeStatus(
                            new ContractStatusCommand(signed.id(), signed.generation(), "NORMAL"), "operator"))
                    .isInstanceOf(CommonException.class);
            var terminated = contracts.changeStatus(
                    new ContractStatusCommand(signed.id(), suspended.generation(), "TERMINATED"), "operator");
            assertThatThrownBy(() -> contracts.changeStatus(
                            new ContractStatusCommand(signed.id(), terminated.generation(), "NORMAL"), "operator"))
                    .isInstanceOf(CommonException.class);
        });
    }

    @Test
    void concurrentDuplicateSigningReturnsOneCommittedContract() throws Exception {
        String tenant = UUID.randomUUID().toString();
        String plan = TenantUtil.callWithTenant(tenant, () -> plan());
        var command = command(plan, "concurrent");
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(() -> TenantUtil.callWithTenant(tenant, () -> contracts.sign(command, "operator")));
            var second =
                    executor.submit(() -> TenantUtil.callWithTenant(tenant, () -> contracts.sign(command, "operator")));
            assertThat(first.get().id()).isEqualTo(second.get().id());
        }
    }

    @Test
    void failedSigningLeavesNoHeaderAndRetirementDoesNotRewriteExistingTerms() {
        TenantUtil.runWithTenant(UUID.randomUUID().toString(), () -> {
            String plan = plan();
            var invalid = new ContractSignCommand(
                    "merchant", "store", plan, "invalid", Set.of("missing"), Map.of(), null, null);
            assertThatThrownBy(() -> contracts.sign(invalid, "operator")).isInstanceOf(IllegalArgumentException.class);
            var signed = contracts.sign(command(plan, "valid"), "operator");
            var version = catalog.detail(plan);
            catalog.retire(new CatalogActionCommand(plan, version.rowVersion()), "operator");
            assertThat(contracts.detail(signed.id()).snapshotHash()).isEqualTo(signed.snapshotHash());
            assertThat(runtime.resolve(new ContractResolveQuery("merchant", "store", "basic.scan", Map.of()))
                            .allowed())
                    .isTrue();
        });
    }

    private ContractSignCommand command(String plan, String request) {
        return new ContractSignCommand("merchant", "store", plan, request, Set.of("basic.scan"), Map.of(), null, null);
    }

    private CatalogVersionDTO product() {
        return publish("PRODUCT", "SCAN", productDefinition());
    }

    private String plan() {
        var product = product();
        var bundle = publish("BUNDLE", "BASIC", bundleDefinition(product.id()));
        return publish(
                        "SALES_PLAN",
                        "STANDARD",
                        Map.of(
                                "bundles",
                                List.of(Map.of("alias", "basic", "bundleId", bundle.id())),
                                "values",
                                Map.of(),
                                "merchantPolicies",
                                Map.of(),
                                "saleStartsAt",
                                Instant.now().minusSeconds(60).toString()))
                .id();
    }

    private CatalogVersionDTO publish(String kind, String code, Map<String, Object> definition) {
        var draft = catalog.save(new CatalogSaveCommand(null, null, kind, code, 1, definition), "operator");
        return catalog.publish(new CatalogActionCommand(draft.id(), draft.rowVersion()), "operator");
    }

    private Map<String, Object> productDefinition() {
        return Map.of(
                "capabilityCode",
                "payment.collect",
                "parameters",
                List.of(Map.of(
                        "key",
                        "fee.rate",
                        "type",
                        "DECIMAL",
                        "required",
                        true,
                        "defaultValue",
                        "0.005",
                        "minimum",
                        "0.003",
                        "maximum",
                        "0.01",
                        "allowedValues",
                        List.of(),
                        "editableLayers",
                        List.of("BUNDLE", "SALES_PLAN", "MERCHANT"))),
                "dependencies",
                List.of(),
                "exclusions",
                List.of());
    }

    private Map<String, Object> bundleDefinition(String productId) {
        return Map.of(
                "items",
                List.of(Map.of(
                        "itemKey",
                        "scan",
                        "productId",
                        productId,
                        "required",
                        true,
                        "defaultSelected",
                        true,
                        "values",
                        Map.of())));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
        @Bean
        MerchantFactsProvider verifiedMerchants() {
            return (tenant, merchant) -> new MerchantProfileDTO(
                    merchant, true, Map.of("merchant.industry", new ValueDTO("STRING", "RETAIL")));
        }
    }
}
