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
import io.github.loadup.modules.log.app.service.OperationLogService;
import io.github.loadup.modules.log.client.dto.LogStatisticsDTO;
import io.github.loadup.modules.log.client.query.LogStatisticsQuery;
import io.github.loadup.modules.log.client.query.OperationLogQuery;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
import java.io.PrintWriter;
import java.io.StringWriter;
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
class OperationLogStatisticsIT {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogGateway operationLogGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM operation_log");
    }

    @Test
    void statistics_shouldReturnCorrectTotals() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u1", "用户管理", "UPDATE", true, 200L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", false, 50L));

        LogStatisticsQuery query = new LogStatisticsQuery();
        LogStatisticsDTO stats = operationLogService.statistics(query);

        assertThat(stats.getTotal()).isEqualTo(3);
        assertThat(stats.getSuccessCount()).isEqualTo(2);
        assertThat(stats.getFailureCount()).isEqualTo(1);
        assertThat(stats.getSuccessRate()).isEqualTo(66.67);
    }

    @Test
    void statistics_shouldGroupByModule() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u1", "用户管理", "UPDATE", true, 200L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", true, 50L));

        LogStatisticsQuery query = new LogStatisticsQuery();
        LogStatisticsDTO stats = operationLogService.statistics(query);

        assertThat(stats.getByModule()).isNotEmpty();
        var moduleMap = stats.getByModule().stream()
                .collect(java.util.stream.Collectors.toMap(
                        LogStatisticsDTO.StatItem::getName, LogStatisticsDTO.StatItem::getCount));
        assertThat(moduleMap.get("用户管理")).isEqualTo(2);
        assertThat(moduleMap.get("角色管理")).isEqualTo(1);
    }

    @Test
    void statistics_shouldGroupByOperationType() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 150L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", true, 50L));

        LogStatisticsQuery query = new LogStatisticsQuery();
        LogStatisticsDTO stats = operationLogService.statistics(query);

        var typeMap = stats.getByOperationType().stream()
                .collect(java.util.stream.Collectors.toMap(
                        LogStatisticsDTO.StatItem::getName, LogStatisticsDTO.StatItem::getCount));
        assertThat(typeMap.get("CREATE")).isEqualTo(2);
        assertThat(typeMap.get("DELETE")).isEqualTo(1);
    }

    @Test
    void statistics_shouldReturnDurationStats() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u1", "用户管理", "UPDATE", true, 500L));

        LogStatisticsQuery query = new LogStatisticsQuery();
        LogStatisticsDTO stats = operationLogService.statistics(query);

        assertThat(stats.getMaxDuration()).isEqualTo(500L);
        assertThat(stats.getAvgDuration()).isBetween(100.0, 500.0);
    }

    @Test
    void statistics_shouldFilterByModule() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", true, 50L));

        LogStatisticsQuery query = new LogStatisticsQuery();
        query.setModule("用户管理");
        LogStatisticsDTO stats = operationLogService.statistics(query);

        assertThat(stats.getTotal()).isEqualTo(1);
    }

    @Test
    void exportCsv_shouldContainAllRows() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", false, 50L));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        operationLogService.exportCsv(new OperationLogQuery(), pw);

        String csv = sw.toString();
        // header + 2 data rows
        List<String> lines = csv.lines().filter(l -> !l.isBlank()).toList();
        assertThat(lines).hasSizeGreaterThanOrEqualTo(3);
        assertThat(lines.getFirst()).contains("用户ID").contains("模块").contains("操作类型");
    }

    @Test
    void exportCsv_shouldFilterBySuccessFalse() {
        operationLogGateway.save(buildLog("u1", "用户管理", "CREATE", true, 100L));
        operationLogGateway.save(buildLog("u2", "角色管理", "DELETE", false, 50L));

        OperationLogQuery query = new OperationLogQuery();
        query.setSuccess(false);
        StringWriter sw = new StringWriter();
        operationLogService.exportCsv(query, new PrintWriter(sw));

        String csv = sw.toString();
        List<String> dataLines =
                csv.lines().filter(l -> !l.isBlank() && !l.startsWith("ID")).toList();
        assertThat(dataLines).hasSize(1);
        assertThat(dataLines.getFirst()).contains("false");
    }

    private OperationLog buildLog(String userId, String module, String type, boolean success, long duration) {
        return OperationLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(userId)
                .module(module)
                .operationType(type)
                .success(success)
                .duration(duration)
                .operationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
