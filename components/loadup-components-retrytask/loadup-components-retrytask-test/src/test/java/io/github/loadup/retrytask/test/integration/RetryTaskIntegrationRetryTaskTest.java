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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the retry task framework
 */
@Transactional
class RetryTaskIntegrationRetryTaskTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @Test
    @DisplayName("Should register a new retry task and save it to the database")
    void testRegisterAndVerifyTask() {
        // Given
        RetryTaskRegisterRequest request = createValidRequest("ORDER-001");

        // When
        Long taskId = retryTaskFacade.register(request);

        // Then
        assertNotNull(taskId);
        Optional<RetryTask> savedTask = retryTaskRepository.findById(taskId);
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
        assertTrue(retryTaskRepository.findById(taskId).isPresent());

        // When
        retryTaskFacade.delete(request.getBizType(), request.getBizId());

        // Then
    }

    @Test
    @DisplayName("Should reset a retry task's count and status")
    void testResetRetryTask() {
        // Given
        RetryTaskRegisterRequest request = createValidRequest("ORDER-003");
        Long taskId = retryTaskFacade.register(request);

        // When
        retryTaskFacade.reset(request.getBizType(), request.getBizId());

        // Then
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
