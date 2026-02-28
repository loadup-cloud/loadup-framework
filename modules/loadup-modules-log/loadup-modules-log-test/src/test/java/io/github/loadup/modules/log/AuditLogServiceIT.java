package io.github.loadup.modules.log;

/*-
 * #%L
 * Loadup Modules Log Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
