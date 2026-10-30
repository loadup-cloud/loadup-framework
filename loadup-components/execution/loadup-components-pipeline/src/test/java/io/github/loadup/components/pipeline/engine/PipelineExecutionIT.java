/*
 * #%L
 * Loadup Components Pipeline
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
package io.github.loadup.components.pipeline.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.pipeline.api.IPipelineDefinition;
import io.github.loadup.components.pipeline.builder.PipelineBuilder;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.exception.ExceptionHandlerBus;
import io.github.loadup.components.pipeline.exception.IExceptionClassHandler;
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.tx.DefaultSpringTxInitializer;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(classes = PipelineExecutionIT.Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
class PipelineExecutionIT {
    private final PipelineExecutor executor;
    private final JdbcTemplate jdbc;

    @Autowired
    PipelineExecutionIT(PipelineExecutor executor, JdbcTemplate jdbc) {
        this.executor = executor;
        this.jdbc = jdbc;
    }

    @BeforeEach
    void createTable() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS pipeline_execution_test (
              id VARCHAR(64) NOT NULL PRIMARY KEY,
              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
              deleted TINYINT NOT NULL DEFAULT 0
            ) ENGINE=InnoDB
            """);
    }

    @Test
    void commitsRealDatabaseWritesBeforeContinuingOutsideTheBlock() {
        String id = UUID.randomUUID().toString();
        PipelineSpec spec = PipelineBuilder.builder()
                .startTx(DefaultSpringTxInitializer.class)
                .process(ctx -> {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                            .isTrue();
                    jdbc.update("INSERT INTO pipeline_execution_test(id) VALUES (?)", id);
                })
                .endTx()
                .process(ctx -> {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                            .isFalse();
                    assertThat(count(id)).isEqualTo(1);
                })
                .assemble(ctx -> ctx.getRequest(String.class))
                .build();
        assertThat(run(spec, ExceptionHandlerBus.builder(), id)).isEqualTo(id);
        assertThat(count(id)).isEqualTo(1);
    }

    @Test
    void handlerRunsAfterRollbackAndCannotAccidentallyCommitPartialWrites() {
        String id = UUID.randomUUID().toString();
        PipelineSpec spec = PipelineBuilder.builder()
                .startTx(DefaultSpringTxInitializer.class)
                .process(ctx -> jdbc.update("INSERT INTO pipeline_execution_test(id) VALUES (?)", id))
                .process(ctx -> {
                    throw new IllegalArgumentException("simulated domain failure");
                })
                .endTx()
                .assemble(ctx -> "unexpected success")
                .build();
        assertThat(run(spec, ExceptionHandlerBus.builder().bottom(FailureHandler.class), id))
                .isEqualTo("failed");
        assertThat(count(id)).isZero();
        assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                .isFalse();
    }

    @Test
    void rollbackDoesNotUndoEarlierCommittedWorkAndPreservesThrownException() {
        String committedId = UUID.randomUUID().toString();
        String rolledBackId = UUID.randomUUID().toString();
        IllegalStateException failure = new IllegalStateException("simulated infrastructure failure");
        PipelineSpec spec = PipelineBuilder.builder()
                .process(ctx -> jdbc.update("INSERT INTO pipeline_execution_test(id) VALUES (?)", committedId))
                .startTx(DefaultSpringTxInitializer.class)
                .process(ctx -> jdbc.update("INSERT INTO pipeline_execution_test(id) VALUES (?)", rolledBackId))
                .process(ctx -> {
                    throw failure;
                })
                .endTx()
                .build();
        assertThatThrownBy(() -> run(spec, ExceptionHandlerBus.builder().overflow(RuntimeException.class), "request"))
                .isSameAs(failure);
        assertThat(count(committedId)).isEqualTo(1);
        assertThat(count(rolledBackId)).isZero();
    }

    private int count(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM pipeline_execution_test WHERE id = ?", Integer.class, id);
    }

    private Object run(PipelineSpec spec, ExceptionHandlerBus bus, String request) {
        IPipelineDefinition definition = new IPipelineDefinition() {
            @Override
            public PipelineSpec definePipeline() {
                return spec;
            }

            @Override
            public ExceptionHandlerBus exceptions() {
                return bus;
            }
        };
        return executor.execute(definition, request);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
        @Bean
        FailureHandler failureHandler() {
            return new FailureHandler();
        }
    }

    static class FailureHandler implements IExceptionClassHandler<String> {
        @Override
        public String assembleResultOnException(Throwable failure, PipelineContext context) {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                    .isFalse();
            return "failed";
        }
    }
}
