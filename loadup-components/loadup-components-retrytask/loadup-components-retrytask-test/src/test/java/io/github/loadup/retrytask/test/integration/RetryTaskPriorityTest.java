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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.facade.enums.Priority;
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

@Transactional
public class RetryTaskPriorityTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskService retryTaskService;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @Test
    @DisplayName("Should prioritize high-priority tasks over low-priority tasks")
    void testTaskPrioritization() {
        // Given: Register a low-priority task, then a high-priority task
        RetryTaskRegisterRequest lowPriorityRequest = createRequest("LOW-001", Priority.LOW);
        retryTaskService.register(lowPriorityRequest);

        RetryTaskRegisterRequest highPriorityRequest = createRequest("HIGH-001", Priority.HIGH);
        retryTaskService.register(highPriorityRequest);

        // Manually update retry times to create a specific scenario where both are ready,
        // but the high-priority one is technically "later"
        RetryTask lowPriorityTask = retryTaskRepository
                .findByBizTypeAndBizId("PRIORITY_TEST", "LOW-001")
                .orElseThrow();
        lowPriorityTask.setNextRetryTime(LocalDateTime.now().minusSeconds(10));
        retryTaskRepository.save(lowPriorityTask);

        RetryTask highPriorityTask = retryTaskRepository
                .findByBizTypeAndBizId("PRIORITY_TEST", "HIGH-001")
                .orElseThrow();
        highPriorityTask.setNextRetryTime(LocalDateTime.now().minusSeconds(5));
        retryTaskRepository.save(highPriorityTask);

        // When: Pull tasks that are ready to be executed
        List<RetryTask> tasks = retryTaskService.pullTasks(10);

        // Then: The high-priority task should be first in the list, despite its later retry time
        assertNotNull(tasks);
        assertEquals(2, tasks.size(), "Should pull both tasks");

        RetryTask firstTask = tasks.get(0);
        assertEquals(Priority.HIGH, firstTask.getPriority(), "The first task should be high priority");
        assertEquals("HIGH-001", firstTask.getBizId());

        RetryTask secondTask = tasks.get(1);
        assertEquals(Priority.LOW, secondTask.getPriority(), "The second task should be low priority");
        assertEquals("LOW-001", secondTask.getBizId());
    }

    private RetryTaskRegisterRequest createRequest(String bizId, Priority priority) {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("PRIORITY_TEST");
        request.setBizId(bizId);
        request.setPriority(priority);
        return request;
    }
}
