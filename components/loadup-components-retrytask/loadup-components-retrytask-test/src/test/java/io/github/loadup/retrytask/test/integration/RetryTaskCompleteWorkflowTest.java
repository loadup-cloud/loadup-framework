package io.github.loadup.retrytask.test.integration;

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

import io.github.loadup.retrytask.core.RetryTaskProcessor;
import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end integration tests covering complete retry workflow
 */
@Transactional
class RetryTaskCompleteWorkflowTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @Autowired
    private RetryTaskService retryTaskService;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    private static final AtomicInteger processCounter = new AtomicInteger(0);
    private static final AtomicInteger failureCounter = new AtomicInteger(0);

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RetryTaskProcessor successAfterRetriesProcessor() {
            return new RetryTaskProcessor() {
                private final AtomicInteger attempts = new AtomicInteger(0);

                @Override
                public String getBizType() {
                    return "SUCCESS_AFTER_RETRIES";
                }

                @Override
                public boolean process(RetryTask task) {
                    int attempt = attempts.incrementAndGet();
                    processCounter.incrementAndGet();
                    // Succeed on 3rd attempt
                    return attempt >= 3;
                }
            };
        }

        @Bean
        public RetryTaskProcessor alwaysFailProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "ALWAYS_FAIL";
                }

                @Override
                public boolean process(RetryTask task) {
                    failureCounter.incrementAndGet();
                    return false;
                }
            };
        }

        @Bean
        public RetryTaskProcessor alwaysSuccessProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "ALWAYS_SUCCESS";
                }

                @Override
                public boolean process(RetryTask task) {
                    return true;
                }
            };
        }
    }

    @Test
    @DisplayName("Complete workflow: Register -> Retry with failures -> Success -> Delete")
    void testCompleteRetryWorkflow() {
        // Register task
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("SUCCESS_AFTER_RETRIES");
        request.setBizId("WORKFLOW-001");
        request.setPriority(Priority.HIGH);

        Long taskId = retryTaskFacade.register(request);
        assertThat(taskId).isNotNull();

        // First attempt - will fail
        retryTaskService.markFailure(taskId, "First failure");
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);

        // Second attempt - will fail
        retryTaskService.markFailure(taskId, "Second failure");
        task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(2);

        // Third attempt - will succeed
        retryTaskService.markSuccess(taskId);
        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should handle max retries and mark as FAILURE")
    void testMaxRetriesReached() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ALWAYS_FAIL");
        request.setBizId("MAX-RETRIES-001");

        Long taskId = retryTaskFacade.register(request);

        // Fail 10 times (default max)
        for (int i = 0; i < 10; i++) {
            RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
            if (task.getStatus() != RetryTaskStatus.FAILURE) {
                retryTaskService.markFailure(taskId, "Failure " + (i + 1));
            }
        }

        RetryTask finalTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(finalTask.getStatus()).isEqualTo(RetryTaskStatus.FAILURE);
        assertThat(finalTask.getRetryCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should prioritize high priority tasks")
    void testTaskPrioritization() {
        // Create low priority task
        RetryTaskRegisterRequest lowRequest = new RetryTaskRegisterRequest();
        lowRequest.setBizType("TEST");
        lowRequest.setBizId("LOW-0001");
        lowRequest.setPriority(Priority.LOW);
        Long lowId = retryTaskFacade.register(lowRequest);

        // Create high priority task
        RetryTaskRegisterRequest highRequest = new RetryTaskRegisterRequest();
        highRequest.setBizType("TEST");
        highRequest.setBizId("HIGH-0001");
        highRequest.setPriority(Priority.HIGH);
        Long highId = retryTaskFacade.register(highRequest);

        // Update both to be due
        RetryTask lowTask = retryTaskRepository.findById(lowId).orElseThrow();
        lowTask.setNextRetryTime(LocalDateTime.now().minusMinutes(10));
        retryTaskRepository.save(lowTask);

        RetryTask highTask = retryTaskRepository.findById(highId).orElseThrow();
        highTask.setNextRetryTime(LocalDateTime.now().minusMinutes(5));
        retryTaskRepository.save(highTask);

        // Pull tasks - high priority should come first
        List<RetryTask> tasks = retryTaskService.pullTasks(10);
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(2);

        // Find our tasks in the list
        RetryTask firstTask = tasks.stream()
                .filter(t -> t.getId().equals(highId) || t.getId().equals(lowId))
                .findFirst()
                .orElseThrow();

        // High priority should be first
        assertThat(firstTask.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Should handle concurrent task execution with locking")
    void testConcurrentExecution() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ALWAYS_SUCCESS");
        request.setBizId("CONCURRENT-001");

        Long taskId = retryTaskFacade.register(request);

        // First lock succeeds
        boolean lock1 = retryTaskService.tryLock(taskId);
        assertThat(lock1).isTrue();

        // Second lock fails (task is RUNNING)
        boolean lock2 = retryTaskService.tryLock(taskId);
        assertThat(lock2).isFalse();

        // Complete the task
        retryTaskService.markSuccess(taskId);
        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should reset and retry tasks")
    void testResetAndRetry() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST");
        request.setBizId("RESET-001");

        Long taskId = retryTaskFacade.register(request);

        // Fail a few times
        retryTaskService.markFailure(taskId, "Failure 1");
        retryTaskService.markFailure(taskId, "Failure 2");

        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(2);

        // Reset the task
        retryTaskService.reset("TEST", "RESET-001");

        task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(0);
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should filter tasks by bizType")
    void testFilterByBizType() {
        // Create tasks of different types
        RetryTaskRegisterRequest typeARequest = new RetryTaskRegisterRequest();
        typeARequest.setBizType("TYPE_A");
        typeARequest.setBizId("A-001");
        Long aId = retryTaskFacade.register(typeARequest);

        RetryTaskRegisterRequest typeBRequest = new RetryTaskRegisterRequest();
        typeBRequest.setBizType("TYPE_B");
        typeBRequest.setBizId("B-001");
        retryTaskFacade.register(typeBRequest);

        // Update TYPE_A to be due
        RetryTask taskA = retryTaskRepository.findById(aId).orElseThrow();
        taskA.setNextRetryTime(LocalDateTime.now().minusMinutes(5));
        retryTaskRepository.save(taskA);

        // Pull only TYPE_A tasks
        List<RetryTask> typeATasks = retryTaskService.pullTasks("TYPE_A", 10);
        assertThat(typeATasks).allMatch(t -> "TYPE_A".equals(t.getBizType()));
    }

    @Test
    @DisplayName("Should handle immediate execution with wait")
    void testImmediateExecutionWithWait() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ALWAYS_SUCCESS");
        request.setBizId("IMMEDIATE-WAIT-001");
        request.setExecuteImmediately(true);
        request.setWaitResult(true);

        Long taskId = retryTaskFacade.register(request);

        // Task should be deleted immediately after successful execution
        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should handle immediate execution without wait")
    void testImmediateExecutionWithoutWait() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ALWAYS_SUCCESS");
        request.setBizId("IMMEDIATE-NOWAIT-001");
        request.setExecuteImmediately(true);
        request.setWaitResult(false);

        Long taskId = retryTaskFacade.register(request);

        // Task should eventually be deleted
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(retryTaskRepository.findById(taskId)).isEmpty();
        });
    }

    @Test
    @DisplayName("Should clean up stuck tasks")
    void testStuckTaskCleanup() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST");
        request.setBizId("STUCK-001");

        Long taskId = retryTaskFacade.register(request);

        // Lock the task
        retryTaskService.tryLock(taskId);

        // Simulate stuck task by updating time
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        task.setUpdateTime(LocalDateTime.now().minusMinutes(20));
        retryTaskRepository.save(task);

        // Reset stuck tasks
        int resetCount = retryTaskService.resetStuckTasks(LocalDateTime.now().minusMinutes(10));
        assertThat(resetCount).isGreaterThanOrEqualTo(1);

        // Task should be back to PENDING
        task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should delete task by bizType and bizId")
    void testDeleteByBizTypeAndBizId() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("DELETE_TEST");
        request.setBizId("DELETE-001");

        Long taskId = retryTaskFacade.register(request);
        assertThat(retryTaskRepository.findById(taskId)).isPresent();

        retryTaskService.delete("DELETE_TEST", "DELETE-001");
        assertThat(retryTaskRepository.findByBizTypeAndBizId("DELETE_TEST", "DELETE-001"))
                .isEmpty();
    }
}
