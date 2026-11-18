/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = TaskSchemaIsolationIT.Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class TaskSchemaIsolationIT {
    private final DataSource administrator;
    private final TaskProperties properties = new TaskProperties();
    private HikariDataSource first;
    private HikariDataSource second;

    @Autowired
    TaskSchemaIsolationIT(DataSource administrator) {
        this.administrator = administrator;
    }

    @BeforeEach
    void provisionIndependentTaskSchemas() throws SQLException {
        script("cleanup.sql");
        script("setup.sql");
        for (String schema : List.of("task_isolation_a", "task_isolation_b")) {
            try (Connection connection = administrator.getConnection()) {
                connection.setSchema(schema);
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/task/pg/schema.sql"));
            }
        }
        script("grants.sql");
        first = pool("task_isolation_a");
        second = pool("task_isolation_b");
        properties.setPollInterval(Duration.ofMillis(100));
    }

    @AfterEach
    void cleanup() {
        if (first != null) first.close();
        if (second != null) second.close();
        script("cleanup.sql");
    }

    @Test
    void workerWithoutRequestContextCannotClaimOrAcknowledgeAnotherDeploymentTask() {
        var repositoryA = repository(first);
        var repositoryB = repository(second);
        String firstId = publish(first, repositoryA, "deployment-a");
        String secondId = publish(second, repositoryB, "deployment-b");
        var seenA = new CopyOnWriteArrayList<String>();
        var seenB = new CopyOnWriteArrayList<String>();
        var workerA = worker(repositoryA, seenA);
        var workerB = worker(repositoryB, seenB);
        try {
            workerA.start();
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(repositoryA.find(firstId)).isEmpty());
            assertThat(seenA).containsExactly("deployment-a");
            assertThat(repositoryA.find(secondId)).isEmpty();
            assertThat(repositoryB.find(secondId).orElseThrow().status()).isEqualTo(TaskStatus.PENDING);
            var claimB = repositoryB.claim().orElseThrow();
            assertThat(repositoryA.complete(claimB)).isFalse();
            assertThat(repositoryB.complete(claimB)).isTrue();
            String nextB = publish(second, repositoryB, "deployment-b-next");
            workerB.start();
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(repositoryB.find(nextB)).isEmpty());
            assertThat(seenB).containsExactly("deployment-b-next");
            assertThat(seenA).containsExactly("deployment-a");
        } finally {
            workerA.stop();
            workerB.stop();
        }
    }

    @Test
    void businessAndTaskRollbackTogetherWithinEachRuntimeDataSource() {
        for (DataSource source : List.of(first, second)) {
            var manager = new DataSourceTransactionManager(source);
            var repository = new JdbcTaskRepository(source, manager, properties);
            var jdbc = new JdbcTemplate(source);
            assertThatThrownBy(() -> new TransactionTemplate(manager).executeWithoutResult(status -> {
                        jdbc.update("INSERT INTO business_probe (id) VALUES (?)", "same-biz");
                        repository.submit(TaskSubmission.immediate("isolation.probe", "same-biz", "{}"));
                        throw new IllegalStateException("force rollback");
                    }))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("force rollback");
            assertThat(repository.pendingCount()).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM business_probe", Integer.class))
                    .isZero();
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                jdbc.update("INSERT INTO business_probe (id) VALUES (?)", "same-biz");
            });
        }
        assertThatThrownBy(() -> new JdbcTaskRepository(first, new DataSourceTransactionManager(second), properties))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void runtimeRolesCannotReadOrChangeTheOtherTaskSchema() throws SQLException {
        denied(first, "SELECT * FROM task_isolation_b.loadup_task");
        denied(first, "UPDATE task_isolation_b.loadup_task SET status = 'CANCELLED'");
        denied(second, "SELECT * FROM task_isolation_a.loadup_task");
        denied(second, "UPDATE task_isolation_a.loadup_task SET status = 'CANCELLED'");
    }

    private TaskWorker worker(JdbcTaskRepository repository, List<String> seen) {
        TaskHandler handler = new TaskHandler() {
            @Override
            public String taskType() {
                return "isolation.probe";
            }

            @Override
            public void handle(TaskExecution event) {
                assertThat(ContextHolder.isEmpty()).isTrue();
                seen.add(event.payload());
            }
        };
        return new TaskWorker(new TaskDispatcher(repository, properties, List.of(handler), null), properties);
    }

    private JdbcTaskRepository repository(DataSource source) {
        return new JdbcTaskRepository(source, new DataSourceTransactionManager(source), properties);
    }

    private static String publish(DataSource source, JdbcTaskRepository repository, String payload) {
        return new TransactionTemplate(new DataSourceTransactionManager(source))
                .execute(status -> repository.submit(TaskSubmission.immediate("isolation.probe", "same-biz", payload)));
    }

    private static HikariDataSource pool(String schema) {
        var config = new HikariConfig();
        config.setJdbcUrl(SharedPostgreSQLContainer.getJdbcUrl());
        config.setUsername(schema);
        config.setPassword("isolation-test-only");
        config.setSchema(schema);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        return new HikariDataSource(config);
    }

    private static void denied(DataSource source, String sql) throws SQLException {
        try (Connection connection = source.getConnection();
                var statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.execute(sql))
                    .isInstanceOfSatisfying(
                            SQLException.class,
                            error -> assertThat(error.getSQLState()).isEqualTo("42501"));
        }
    }

    private void script(String file) {
        new ResourceDatabasePopulator(new ClassPathResource("db/task-isolation/" + file)).execute(administrator);
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(DataSourceAutoConfiguration.class)
    static class Application {}
}
