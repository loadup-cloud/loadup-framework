package io.github.loadup.retrytask.test.core;

/*-
 * #%L
 * Loadup Components Retrytask Test
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
import static org.awaitility.Awaitility.await;

import io.github.loadup.retrytask.core.RetryTaskExecutor;
import io.github.loadup.retrytask.core.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for RetryTaskExecutor
 */
@Transactional
class RetryTaskExecutorTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskExecutor retryTaskExecutor;

    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RetryTaskProcessor testSuccessProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "TEST_SUCCESS";
                }

                @Override
                public boolean process(RetryTask task) {
                    return true;
                }
            };
        }

        @Bean
        public RetryTaskProcessor testFailureProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "TEST_FAILURE";
                }

                @Override
                public boolean process(RetryTask task) {
                    return false;
                }
            };
        }

        @Bean
        public RetryTaskProcessor testExceptionProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "TEST_EXCEPTION";
                }

                @Override
                public boolean process(RetryTask task) {
                    throw new RuntimeException("Processing error");
                }
            };
        }

        @Bean
        public RetryTaskProcessor testCounterProcessor() {
            return new RetryTaskProcessor() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public String getBizType() {
                    return "TEST_COUNTER";
                }

                @Override
                public boolean process(RetryTask task) {
                    counter.incrementAndGet();
                    return true;
                }

                public int getCount() {
                    return counter.get();
                }
            };
        }
    }

    @Test
    @DisplayName("Should execute successfully and delete task")
    void executeSyncShouldProcessAndMarkSuccess() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST_SUCCESS");
        request.setBizId("SUCCESS-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        retryTaskExecutor.executeSync(task);

        // Task should be deleted after success
        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should mark failure when processor returns false")
    void executeSyncShouldMarkFailureWhenProcessorFails() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST_FAILURE");
        request.setBizId("FAILURE-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        retryTaskExecutor.executeSync(task);

        // Task should still exist with incremented retry count
        RetryTask failedTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(failedTask.getRetryCount()).isEqualTo(1);
        assertThat(failedTask.getLastFailureReason()).contains("returned false");
    }

    @Test
    @DisplayName("Should mark failure when processor throws exception")
    void executeSyncShouldMarkFailureOnException() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST_EXCEPTION");
        request.setBizId("EXCEPTION-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        retryTaskExecutor.executeSync(task);

        RetryTask failedTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(failedTask.getRetryCount()).isEqualTo(1);
        assertThat(failedTask.getLastFailureReason()).contains("Processing error");
    }

    @Test
    @DisplayName("Should skip execution if lock fails")
    void executeSyncShouldSkipIfAlreadyLocked() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST_SUCCESS");
        request.setBizId("LOCKED-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        // Lock the task manually
        task.setStatus(RetryTaskStatus.RUNNING);
        retryTaskRepository.save(task);

        // Try to execute - should skip
        retryTaskExecutor.executeSync(task);

        // Task should still be in RUNNING state (not deleted)
        RetryTask lockedTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(lockedTask.getStatus()).isEqualTo(RetryTaskStatus.RUNNING);
    }

    @Test
    @DisplayName("Should mark failure when no processor found")
    void executeSyncShouldMarkFailureWhenNoProcessor() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("NO_PROCESSOR");
        request.setBizId("NO-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        retryTaskExecutor.executeSync(task);

        RetryTask failedTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(failedTask.getRetryCount()).isEqualTo(1);
        assertThat(failedTask.getLastFailureReason()).contains("No processor found");
    }

    @Test
    @DisplayName("Should execute async in background")
    void executeAsyncShouldRunInBackground() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST_COUNTER");
        request.setBizId("ASYNC-001");

        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();

        retryTaskExecutor.executeAsync(task);

        // Wait for async execution to complete
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(retryTaskRepository.findById(taskId)).isEmpty();
        });
    }
}
