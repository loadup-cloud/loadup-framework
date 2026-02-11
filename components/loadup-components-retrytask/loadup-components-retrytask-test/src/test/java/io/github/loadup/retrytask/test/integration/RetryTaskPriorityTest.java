package io.github.loadup.retrytask.test.integration;

import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
public class RetryTaskPriorityTest {


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
        RetryTask lowPriorityTask = retryTaskRepository.findByBizTypeAndBizId("PRIORITY_TEST", "LOW-001").orElseThrow();
        lowPriorityTask.setNextRetryTime(LocalDateTime.now().minusSeconds(10));
        retryTaskRepository.save(lowPriorityTask);

        RetryTask highPriorityTask = retryTaskRepository.findByBizTypeAndBizId("PRIORITY_TEST", "HIGH-001").orElseThrow();
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
