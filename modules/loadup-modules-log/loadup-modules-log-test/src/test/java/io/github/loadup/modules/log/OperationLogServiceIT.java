package io.github.loadup.modules.log;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.modules.log.app.service.OperationLogService;
import io.github.loadup.modules.log.client.dto.OperationLogDTO;
import io.github.loadup.modules.log.client.query.OperationLogQuery;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
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
class OperationLogServiceIT {

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
    void save_shouldPersist_whenValidLog() {
        OperationLog log = buildLog("u001", "alice", "用户管理", "DELETE");
        operationLogGateway.save(log);

        List<OperationLogDTO> results = operationLogService.listByCondition(new OperationLogQuery());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void listByCondition_shouldFilterByUserId() {
        operationLogGateway.save(buildLog("u001", "alice", "用户管理", "CREATE"));
        operationLogGateway.save(buildLog("u002", "bob", "角色管理", "UPDATE"));

        OperationLogQuery query = new OperationLogQuery();
        query.setUserId("u001");
        List<OperationLogDTO> results = operationLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo("u001");
    }

    @Test
    void listByCondition_shouldFilterByModule() {
        operationLogGateway.save(buildLog("u001", "alice", "用户管理", "CREATE"));
        operationLogGateway.save(buildLog("u002", "bob", "角色管理", "DELETE"));

        OperationLogQuery query = new OperationLogQuery();
        query.setModule("角色管理");
        List<OperationLogDTO> results = operationLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getModule()).isEqualTo("角色管理");
    }

    @Test
    void listByCondition_shouldFilterBySuccess() {
        operationLogGateway.save(buildLog("u001", "alice", "用户管理", "CREATE", true));
        operationLogGateway.save(buildLog("u002", "bob", "角色管理", "DELETE", false));

        OperationLogQuery query = new OperationLogQuery();
        query.setSuccess(false);
        List<OperationLogDTO> results = operationLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSuccess()).isFalse();
    }

    @Test
    void countByCondition_shouldReturnCorrectCount() {
        operationLogGateway.save(buildLog("u001", "alice", "用户管理", "CREATE"));
        operationLogGateway.save(buildLog("u001", "alice", "用户管理", "UPDATE"));
        operationLogGateway.save(buildLog("u002", "bob", "角色管理", "DELETE"));

        OperationLogQuery query = new OperationLogQuery();
        query.setUserId("u001");
        long count = operationLogService.countByCondition(query);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void listByCondition_shouldRespectPagination() {
        for (int i = 0; i < 5; i++) {
            operationLogGateway.save(buildLog("u001", "alice", "用户管理", "QUERY"));
        }

        OperationLogQuery query = new OperationLogQuery();
        query.setPageNum(1);
        query.setPageSize(3);
        List<OperationLogDTO> page1 = operationLogService.listByCondition(query);

        query.setPageNum(2);
        List<OperationLogDTO> page2 = operationLogService.listByCondition(query);

        assertThat(page1).hasSize(3);
        assertThat(page2).hasSize(2);
    }

    @Test
    void record_shouldPersistAsyncLog() throws InterruptedException {
        operationLogService.record("u001", "alice", "用户管理", "LOGIN", "用户登录", true, null, 100L);

        // wait for async write
        Thread.sleep(500);

        OperationLogQuery query = new OperationLogQuery();
        query.setUserId("u001");
        List<OperationLogDTO> results = operationLogService.listByCondition(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOperationType()).isEqualTo("LOGIN");
    }

    private OperationLog buildLog(String userId, String username, String module, String type) {
        return buildLog(userId, username, module, type, true);
    }

    private OperationLog buildLog(String userId, String username, String module, String type, boolean success) {
        return OperationLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(username)
                .module(module)
                .operationType(type)
                .description(type + " operation")
                .success(success)
                .duration(50L)
                .operationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}

