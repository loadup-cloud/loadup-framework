package io.github.loadup.retrytask.test.integration;

import io.github.loadup.retrytask.facade.model.Priority;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.api.management.RetryTaskManagement;
import io.github.loadup.retrytask.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the retry task framework
 */
@Transactional
class RetryTaskIntegrationTest extends BaseTest {


    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @Autowired
    private RetryTaskManagement retryTaskManagement;



    @Test
    @DisplayName("Should register a new retry task and save it to the database")
    void testRegisterAndVerifyTask() {
        // Given
        RetryTaskRegisterRequest request = createValidRequest("ORDER-001");

        // When
        Long taskId = retryTaskFacade.register(request);

        // Then
        assertNotNull(taskId);
        Optional<RetryTask> savedTask = retryTaskManagement.findById(taskId);
        assertTrue(savedTask.isPresent());
        assertEquals(request.getBizType(), savedTask.get().getBizType());
        assertEquals(request.getBizId(), savedTask.get().getBizId());
    }

    @Test
    @DisplayName("Should delete a retry task from the database")
    void testDeleteRetryTask() {
        // Given
        RetryTaskRegisterRequest request = createValidRequest("ORDER-002");
        Long taskId = retryTaskFacade.register(request);
        assertTrue(retryTaskManagement.findById(taskId).isPresent());

        // When
        retryTaskFacade.delete(request.getBizType(), request.getBizId());

        // Then
        assertFalse(retryTaskManagement.findById(taskId).isPresent());
    }

    @Test
    @DisplayName("Should reset a retry task's count and status")
    void testResetRetryTask() {
        // Given
        RetryTaskRegisterRequest request = createValidRequest("ORDER-003");
        Long taskId = retryTaskFacade.register(request);
        RetryTask task = retryTaskManagement.findById(taskId).orElseThrow();
        task.setRetryCount(5);
        retryTaskManagement.save(task);

        // When
        retryTaskFacade.reset(request.getBizType(), request.getBizId());

        // Then
        RetryTask resetTask = retryTaskManagement.findById(taskId).orElseThrow();
        assertEquals(0, resetTask.getRetryCount());
        assertEquals(RetryTaskStatus.PENDING, resetTask.getStatus());
    }

    private RetryTaskRegisterRequest createValidRequest(String bizId) {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("ORDER");
        request.setBizId(bizId);
        request.setPriority(Priority.HIGH);
        request.setNextRetryTime(LocalDateTime.now().plusMinutes(10));
        return request;
    }
}
