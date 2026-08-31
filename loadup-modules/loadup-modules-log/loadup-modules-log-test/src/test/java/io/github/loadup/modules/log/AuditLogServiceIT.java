package io.github.loadup.modules.log;

/*-
 * #%L
 * Loadup Modules Log Test
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

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.modules.log.app.service.AuditLogService;
import io.github.loadup.modules.log.client.dto.AuditLogDTO;
import io.github.loadup.modules.log.client.query.AuditLogQuery;
import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = LogTestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class AuditLogServiceIT {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogGateway auditLogGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM audit_log");
    }

    @Test
    void save_shouldPersist_whenValidLog() {
        auditLogGateway.save(buildLog("u001", "alice", "USER", "u999", "UPDATE"));

        List<AuditLogDTO> results = auditLogService.listByCondition(new AuditLogQuery());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDataType()).isEqualTo("USER");
    }

    @Test
    void listByCondition_shouldFilterByDataType() {
        auditLogGateway.save(buildLog("u001", "alice", "USER", "u001", "UPDATE"));
        auditLogGateway.save(buildLog("u001", "alice", "ROLE", "r001", "ASSIGN"));

        AuditLogQuery query = new AuditLogQuery();
        query.setDataType("ROLE");
        List<AuditLogDTO> results = auditLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAction()).isEqualTo("ASSIGN");
    }

    @Test
    void listByCondition_shouldFilterByDataId() {
        auditLogGateway.save(buildLog("u001", "alice", "USER", "target-id-1", "CREATE"));
        auditLogGateway.save(buildLog("u002", "bob", "USER", "target-id-2", "DELETE"));

        AuditLogQuery query = new AuditLogQuery();
        query.setDataId("target-id-1");
        List<AuditLogDTO> results = auditLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDataId()).isEqualTo("target-id-1");
    }

    @Test
    void countByCondition_shouldReturnCorrectCount() {
        auditLogGateway.save(buildLog("u001", "alice", "USER", "d1", "CREATE"));
        auditLogGateway.save(buildLog("u001", "alice", "USER", "d2", "UPDATE"));
        auditLogGateway.save(buildLog("u002", "bob", "ROLE", "r1", "ASSIGN"));

        AuditLogQuery query = new AuditLogQuery();
        query.setDataType("USER");
        long count = auditLogService.countByCondition(query);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void record_shouldPersistAsyncAuditLog() throws InterruptedException {
        auditLogService.record(
                "u001",
                "alice",
                "CONFIG",
                "c001",
                "UPDATE",
                "{\"value\":\"old\"}",
                "{\"value\":\"new\"}",
                "bug fix",
                "127.0.0.1");

        Thread.sleep(500);

        AuditLogQuery query = new AuditLogQuery();
        query.setDataType("CONFIG");
        List<AuditLogDTO> results = auditLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBeforeData()).isEqualTo("{\"value\":\"old\"}");
        assertThat(results.get(0).getAfterData()).isEqualTo("{\"value\":\"new\"}");
    }

    private AuditLog buildLog(String userId, String username, String dataType, String dataId, String action) {
        return AuditLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(username)
                .dataType(dataType)
                .dataId(dataId)
                .action(action)
                .operationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
