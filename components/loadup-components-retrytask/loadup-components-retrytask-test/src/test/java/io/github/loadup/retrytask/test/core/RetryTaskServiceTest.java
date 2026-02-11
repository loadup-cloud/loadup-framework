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

import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for RetryTaskService
 */
@Transactional
class RetryTaskServiceTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskService retryTaskService;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @Test
    @DisplayName("Should register task successfully")
    void registerShouldSaveTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-001");
        request.setPriority(Priority.HIGH);

        Long taskId = retryTaskService.register(request);

        assertThat(taskId).isNotNull();
        RetryTask savedTask = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(savedTask.getBizType()).isEqualTo("ORDER");
        assertThat(savedTask.getBizId()).isEqualTo("ORDER-001");
        assertThat(savedTask.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
        assertThat(savedTask.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void deleteShouldRemoveTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-002");

        Long taskId = retryTaskService.register(request);
        assertThat(retryTaskRepository.findById(taskId)).isPresent();

        retryTaskService.delete("ORDER", "ORDER-002");
        assertThat(retryTaskRepository.findByBizTypeAndBizId("ORDER", "ORDER-002"))
                .isEmpty();
    }

    @Test
    @DisplayName("Should reset task successfully")
    void resetShouldResetRetryCount() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-003");

        Long taskId = retryTaskService.register(request);

        // Simulate failure
        retryTaskService.markFailure(taskId, "First failure");
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(1);

        // Reset
        retryTaskService.reset("ORDER", "ORDER-003");
        task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(0);
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should pull pending tasks")
    void pullTasksShouldReturnPendingTasks() {
        // Create tasks with past retry time
        for (int i = 0; i < 3; i++) {
            RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
            request.setBizType("BATCH");
            request.setBizId("BATCH-00" + i);
            Long taskId = retryTaskService.register(request);

            // Update to past time
            RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
            task.setNextRetryTime(LocalDateTime.now().minusMinutes(5));
            retryTaskRepository.save(task);
        }

        List<RetryTask> tasks = retryTaskService.pullTasks(10);
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should pull tasks by bizType")
    void pullTasksByBizTypeShouldFilterByType() {
        // Create tasks of different types
        RetryTaskRegisterRequest request1 = new RetryTaskRegisterRequest();
        request1.setBizType("TYPE_A");
        request1.setBizId("A-001");
        Long id1 = retryTaskService.register(request1);

        RetryTaskRegisterRequest request2 = new RetryTaskRegisterRequest();
        request2.setBizType("TYPE_B");
        request2.setBizId("B-001");
        retryTaskService.register(request2);

        // Update to past time
        RetryTask task = retryTaskRepository.findById(id1).orElseThrow();
        task.setNextRetryTime(LocalDateTime.now().minusMinutes(5));
        retryTaskRepository.save(task);

        List<RetryTask> tasks = retryTaskService.pullTasks("TYPE_A", 10);
        assertThat(tasks).allMatch(t -> "TYPE_A".equals(t.getBizType()));
    }

    @Test
    @DisplayName("Should mark task as success and delete it")
    void markSuccessShouldDeleteTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-SUCCESS");

        Long taskId = retryTaskService.register(request);
        retryTaskService.markSuccess(taskId);

        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should increment retry count on failure")
    void markFailureShouldIncrementCount() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-FAIL");

        Long taskId = retryTaskService.register(request);

        retryTaskService.markFailure(taskId, "Network error");

        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getLastFailureReason()).isEqualTo("Network error");
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should mark as FAILURE when max retries reached")
    void markFailureShouldMarkFailedWhenMaxReached() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-MAX-FAIL");

        Long taskId = retryTaskService.register(request);

        // Fail 10 times (default max is 10)
        for (int i = 0; i < 10; i++) {
            retryTaskService.markFailure(taskId, "Failure " + i);
        }

        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.FAILURE);
        assertThat(task.getRetryCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should lock task successfully")
    void tryLockShouldSucceedForPendingTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-LOCK");

        Long taskId = retryTaskService.register(request);

        boolean locked = retryTaskService.tryLock(taskId);
        assertThat(locked).isTrue();

        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.RUNNING);

        // Second lock should fail
        boolean lockedAgain = retryTaskService.tryLock(taskId);
        assertThat(lockedAgain).isFalse();
    }

    @Test
    @DisplayName("Should reset stuck tasks")
    void resetStuckTasksShouldResetRunningTasks() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId("ORDER-STUCK");

        Long taskId = retryTaskService.register(request);

        // Lock the task
        retryTaskService.tryLock(taskId);

        // Manually update time to simulate stuck task
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        task.setUpdateTime(LocalDateTime.now().minusMinutes(10));
        retryTaskRepository.save(task);

        int count = retryTaskService.resetStuckTasks(LocalDateTime.now().minusMinutes(5));
        assertThat(count).isGreaterThanOrEqualTo(1);

        task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }
}
