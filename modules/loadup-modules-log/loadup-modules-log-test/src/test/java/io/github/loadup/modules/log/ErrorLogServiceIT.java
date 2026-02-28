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
import io.github.loadup.modules.log.app.service.ErrorLogService;
import io.github.loadup.modules.log.client.dto.ErrorLogDTO;
import io.github.loadup.modules.log.client.query.ErrorLogQuery;
import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.model.ErrorLog;
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
class ErrorLogServiceIT {

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    private ErrorLogGateway errorLogGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM error_log");
    }

    @Test
    void save_shouldPersist_whenValidLog() {
        errorLogGateway.save(buildLog("SYSTEM", "NullPointerException", "NPE occurred"));

        List<ErrorLogDTO> results = errorLogService.listByCondition(new ErrorLogQuery());
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getErrorType()).isEqualTo("SYSTEM");
    }

    @Test
    void listByCondition_shouldFilterByErrorType() {
        errorLogGateway.save(buildLog("SYSTEM", "NPE", "null error"));
        errorLogGateway.save(buildLog("BUSINESS", "INVALID_PARAM", "invalid input"));

        ErrorLogQuery query = new ErrorLogQuery();
        query.setErrorType("BUSINESS");
        List<ErrorLogDTO> results = errorLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getErrorCode()).isEqualTo("INVALID_PARAM");
    }

    @Test
    void listByCondition_shouldFilterByErrorCode() {
        errorLogGateway.save(buildLog("SYSTEM", "ERR_001", "err 1"));
        errorLogGateway.save(buildLog("SYSTEM", "ERR_002", "err 2"));

        ErrorLogQuery query = new ErrorLogQuery();
        query.setErrorCode("ERR_001");
        List<ErrorLogDTO> results = errorLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getErrorMessage()).isEqualTo("err 1");
    }

    @Test
    void countByCondition_shouldReturnCorrectCount() {
        errorLogGateway.save(buildLog("SYSTEM", "E1", "sys err 1"));
        errorLogGateway.save(buildLog("SYSTEM", "E2", "sys err 2"));
        errorLogGateway.save(buildLog("BUSINESS", "B1", "biz err"));

        ErrorLogQuery query = new ErrorLogQuery();
        query.setErrorType("SYSTEM");
        assertThat(errorLogService.countByCondition(query)).isEqualTo(2);
    }

    @Test
    void record_shouldPersistAsyncErrorLog() throws InterruptedException {
        errorLogService.record(
                "u001",
                "BUSINESS",
                "VALIDATION_FAILED",
                "Field 'name' is required",
                null,
                "/api/user/create",
                "POST",
                "127.0.0.1");

        Thread.sleep(500);

        ErrorLogQuery query = new ErrorLogQuery();
        query.setErrorType("BUSINESS");
        List<ErrorLogDTO> results = errorLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getErrorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(results.getFirst().getRequestUrl()).isEqualTo("/api/user/create");
    }

    @Test
    void recordException_shouldExtractStackTrace() throws InterruptedException {
        RuntimeException ex = new RuntimeException("Something went wrong");
        errorLogService.recordException("u002", ex, "/api/test", "POST", "192.168.1.1");

        Thread.sleep(500);

        ErrorLogQuery query = new ErrorLogQuery();
        query.setErrorCode("RuntimeException");
        List<ErrorLogDTO> results = errorLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getStackTrace()).contains("RuntimeException");
    }

    private ErrorLog buildLog(String type, String code, String message) {
        return ErrorLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .errorType(type)
                .errorCode(code)
                .errorMessage(message)
                .errorTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
