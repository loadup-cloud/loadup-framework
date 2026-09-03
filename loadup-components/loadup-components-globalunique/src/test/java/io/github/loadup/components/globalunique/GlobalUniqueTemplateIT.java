/*-
 * #%L
 * LoadUp Components :: Global Unique
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

package io.github.loadup.components.globalunique;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
@DisplayName("GlobalUniqueTemplate integration tests")
class GlobalUniqueTemplateIT {
    private final GlobalUniqueTemplate template;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    GlobalUniqueTemplateIT(GlobalUniqueTemplate template, PlatformTransactionManager transactionManager) {
        this.template = template;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    @DisplayName("The first tenant-scoped claim wins")
    void claimReturnsTrueOnlyOnce() {
        String uniqueKey = "order-once";

        assertThat(withTenant("tenant-a", () -> template.claim("ORDER", uniqueKey)))
                .isTrue();
        assertThat(withTenant("tenant-a", () -> template.claim("ORDER", uniqueKey)))
                .isFalse();
    }

    @Test
    @DisplayName("The same business key is isolated by tenant and business type")
    void claimIsIsolatedByTenantAndBusinessType() {
        String uniqueKey = "shared-key";

        assertThat(withTenant("tenant-a", () -> template.claim("ORDER", uniqueKey)))
                .isTrue();
        assertThat(withTenant("tenant-b", () -> template.claim("ORDER", uniqueKey)))
                .isTrue();
        assertThat(withTenant("tenant-a", () -> template.claim("PAYMENT", uniqueKey)))
                .isTrue();
    }

    @Test
    @DisplayName("Claim details and database-managed standard fields are readable")
    void findReturnsClaimAndAuditFields() {
        GlobalUniqueClaim claim = new GlobalUniqueClaim("REFUND", "refund-details", "refund-42", "{\"amount\":100}");

        assertThat(withTenant("tenant-details", () -> template.claim(claim))).isTrue();
        GlobalUniqueRecord record = withTenant(
                        "tenant-details", () -> template.find(claim.bizType(), claim.uniqueKey()))
                .orElseThrow();

        assertThat(record.id()).isNotBlank();
        assertThat(record.tenantId()).isEqualTo("tenant-details");
        assertThat(record.bizId()).isEqualTo("refund-42");
        assertThat(record.requestData()).isEqualTo("{\"amount\":100}");
        assertThat(record.createdAt()).isNotNull();
        assertThat(record.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("A rolled-back claim can be retried")
    void rolledBackClaimCanBeRetried() {
        String uniqueKey = "rollback-retry";

        assertThatThrownBy(() -> withTenant("tenant-tx", () -> {
                    transactionTemplate.executeWithoutResult(status -> {
                        assertThat(template.claim("ORDER", uniqueKey)).isTrue();
                        throw new IllegalStateException("rollback");
                    });
                    return null;
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(withTenant("tenant-tx", () -> template.claim("ORDER", uniqueKey)))
                .isTrue();
    }

    @Test
    @DisplayName("Concurrent claims have exactly one winner")
    void concurrentClaimsHaveOneWinner() throws Exception {
        String uniqueKey = "concurrent-once";
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> futures = List.of(
                    executor.submit(() -> concurrentClaim(ready, start, uniqueKey)),
                    executor.submit(() -> concurrentClaim(ready, start, uniqueKey)));
            ready.await();
            start.countDown();

            assertThat(futures).extracting(Future::get).containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Blank business dimensions are rejected")
    void blankDimensionsAreRejected() {
        assertThatThrownBy(() -> template.claim(" ", "key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bizType");
        assertThatThrownBy(() -> template.claim("ORDER", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uniqueKey");
    }

    private boolean concurrentClaim(CountDownLatch ready, CountDownLatch start, String uniqueKey)
            throws InterruptedException {
        ready.countDown();
        start.await();
        return withTenant("tenant-concurrent", () -> template.claim("ORDER", uniqueKey));
    }

    private static <T> T withTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantUtil.getTenantId();
        try {
            TenantUtil.setTenantId(tenantId);
            return action.get();
        } finally {
            if (previousTenantId == null) {
                TenantUtil.clear();
            } else {
                TenantUtil.setTenantId(previousTenantId);
            }
        }
    }
}
