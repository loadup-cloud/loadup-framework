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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
        classes = TaskIT.Application.class,
        properties = {
            "spring.flyway.enabled=false",
            "loadup.task.db-type=mysql",
            "loadup.task.table-prefix=ops_",
            "loadup.task.worker-enabled=false"
        })
@EnableTestContainers(value = ContainerType.MYSQL, reuse = false)
class TaskMySqlIT {
    @Test
    void prefixedQueueDeduplicatesAndDeletesOnlyTheCurrentClaim(
            @Autowired DataSource source,
            @Autowired JdbcTaskRepository tasks,
            @Autowired PlatformTransactionManager manager)
            throws IOException {
        var ddl = new ClassPathResource("db/task/mysql/schema.sql");
        try (var input = ddl.getInputStream()) {
            var script = new String(input.readAllBytes(), StandardCharsets.UTF_8).replace("loadup_", "ops_");
            new ResourceDatabasePopulator(new ByteArrayResource(script.getBytes(StandardCharsets.UTF_8)))
                    .execute(source);
        }

        var transaction = new TransactionTemplate(manager);
        var jdbc = new JdbcTemplate(source);
        var deadline = jdbc.queryForObject("SELECT CURRENT_TIMESTAMP - INTERVAL 1 HOUR", LocalDateTime.class);
        var submission = TaskSubmission.scheduled("order.expire", "order", deadline);
        String id = transaction.execute(status -> tasks.submit(submission));
        String duplicate = transaction.execute(status -> tasks.submit(submission));
        assertThat(duplicate).isEqualTo(id);
        assertThatThrownBy(() -> transaction.execute(
                        status -> tasks.submit(new TaskSubmission("order.expire", "order", "different", deadline))))
                .isInstanceOf(CommonException.class);

        assertThat(tasks.claim(List.of("ORDER.EXPIRE"))).isEmpty();
        assertThat(tasks.find(id).orElseThrow().attempts()).isZero();
        var previous = tasks.claim(List.of("another.type", "order.expire")).orElseThrow();
        jdbc.update("UPDATE ops_task SET lease_until = CURRENT_TIMESTAMP(6) - INTERVAL 1 SECOND WHERE id = ?", id);
        var current = tasks.claim(List.of("order.expire")).orElseThrow();
        assertThat(current.attempt()).isEqualTo(2);
        assertThat(tasks.complete(previous)).isFalse();
        assertThat(tasks.complete(current)).isTrue();
        assertThat(tasks.find(id)).isEmpty();
        assertThat(tasks.fail(previous, new IllegalStateException("stale"))).isFalse();
        String resubmitted = transaction.execute(status -> tasks.submit(submission));
        assertThat(resubmitted).isNotEqualTo(id);
        var retry = tasks.claim(List.of("order.expire")).orElseThrow();
        assertThat(tasks.fail(retry, new IllegalStateException("retry"))).isTrue();
        var pending = tasks.find(resubmitted).orElseThrow();
        assertThat(pending.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(pending.nextExecuteAt()).isAfter(retry.event().nextExecuteAt());
    }
}
