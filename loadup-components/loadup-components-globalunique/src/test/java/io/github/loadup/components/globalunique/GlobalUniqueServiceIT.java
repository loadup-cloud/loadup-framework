package io.github.loadup.components.globalunique;

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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.globalunique.service.GlobalUniqueService;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
@DisplayName("GlobalUniqueService integration tests")
class GlobalUniqueServiceIT {
    private static final Logger log = LoggerFactory.getLogger(GlobalUniqueServiceIT.class);

    @Autowired
    private GlobalUniqueService globalUniqueService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Cleanup test data to avoid dirty data affecting test results
        jdbcTemplate.execute("DELETE FROM global_unique");
        log.debug("Cleaned global_unique table data");
    }

    @Test
    @DisplayName("First insert claims the unique key")
    void insertAndCheck_shouldReturnTrue_whenFirstTime() {
        // given
        String uniqueKey = "TEST_ORDER_CREATE:user123:order456";
        String bizType = "ORDER";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType);

        // then
        assertThat(result).isTrue();

        // Verify entry exists in database
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?", Integer.class, uniqueKey);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Duplicate insert is rejected as idempotent replay")
    void insertAndCheck_shouldReturnFalse_whenDuplicate() {
        // given
        String uniqueKey = "TEST_ORDER_CREATE:user123:order789";
        String bizType = "ORDER";

        // First insert
        boolean firstResult = globalUniqueService.insertAndCheck(uniqueKey, bizType);
        assertThat(firstResult).isTrue();

        // when - second insert (duplicate)
        boolean secondResult = globalUniqueService.insertAndCheck(uniqueKey, bizType);

        // then
        assertThat(secondResult).isFalse();

        // Verify only one record exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?", Integer.class, uniqueKey);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Insert with bizId persists the business id")
    void insertAndCheck_shouldSucceed_withBizId() {
        // given
        String uniqueKey = "TEST_PAYMENT:user456:payment123";
        String bizType = "PAYMENT";
        String bizId = "payment123";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType, bizId);

        // then
        assertThat(result).isTrue();

        // Verify biz_id field
        String savedBizId = jdbcTemplate.queryForObject(
                "SELECT biz_id FROM global_unique WHERE unique_key = ?", String.class, uniqueKey);
        assertThat(savedBizId).isEqualTo(bizId);
    }

    @Test
    @DisplayName("Insert with request data persists the snapshot")
    void insertAndCheck_shouldSucceed_withRequestData() {
        // given
        String uniqueKey = "TEST_REFUND:user789:refund456";
        String bizType = "REFUND";
        String bizId = "refund456";
        String requestData = "{\"userId\":\"user789\",\"amount\":100.00}";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType, bizId, requestData);

        // then
        assertThat(result).isTrue();

        // Verify request_data field
        String savedRequestData = jdbcTemplate.queryForObject(
                "SELECT request_data FROM global_unique WHERE unique_key = ?", String.class, uniqueKey);
        assertThat(savedRequestData).isEqualTo(requestData);
    }

    @Test
    @DisplayName("Concurrent inserts of the same unique key succeed only once")
    void insertAndCheck_shouldOnlyOneSucceed_whenConcurrent() throws InterruptedException {
        // given
        String uniqueKey = "TEST_CONCURRENT:user999:order999";
        String bizType = "ORDER";

        int[] successCount = {0};
        int[] failureCount = {0};

        // when - simulate concurrent insert
        Thread t1 = new Thread(() -> {
            if (globalUniqueService.insertAndCheck(uniqueKey, bizType)) {
                successCount[0]++;
            } else {
                failureCount[0]++;
            }
        });

        Thread t2 = new Thread(() -> {
            if (globalUniqueService.insertAndCheck(uniqueKey, bizType)) {
                successCount[0]++;
            } else {
                failureCount[0]++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // then - only one should succeed
        assertThat(successCount[0]).isEqualTo(1);
        assertThat(failureCount[0]).isEqualTo(1);

        // Verify only one record in database
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?", Integer.class, uniqueKey);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Different unique keys coexist regardless of bizType")
    void insertAndCheck_shouldAllowSameUniqueKey_forDifferentBizType() {
        // given
        String uniqueKey1 = "TEST_COMMON_KEY:user111";
        String uniqueKey2 = "TEST_COMMON_KEY:user222";
        String bizType1 = "ORDER";
        String bizType2 = "PAYMENT";

        // when
        boolean result1 = globalUniqueService.insertAndCheck(uniqueKey1, bizType1);
        boolean result2 = globalUniqueService.insertAndCheck(uniqueKey2, bizType2);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();

        // Verify two records exist
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key LIKE 'TEST_COMMON_KEY%'", Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Inserted rows carry the standard tenant/deleted columns")
    void insert_shouldPersistStandardColumns() {
        // given
        String uniqueKey = "TEST_STANDARD_COLUMNS:user123:order456";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, "ORDER");

        // then
        assertThat(result).isTrue();
        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM global_unique WHERE unique_key = ?", Integer.class, uniqueKey);
        assertThat(deleted).isZero();
        Integer tenantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ? AND tenant_id IS NULL",
                Integer.class,
                uniqueKey);
        assertThat(tenantCount).isEqualTo(1);
    }
}
