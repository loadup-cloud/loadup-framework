package io.github.loadup.retrytask.test.facade;

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
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.Duration;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for RetryTaskFacade
 */
@Transactional
class RetryTaskFacadeTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RetryTaskProcessor facadeTestProcessor() {
            return new RetryTaskProcessor() {
                @Override
                public String getBizType() {
                    return "FACADE_TEST";
                }

                @Override
                public boolean process(RetryTask task) {
                    return true;
                }
            };
        }
    }

    @Test
    @DisplayName("Should register task with default behavior")
    void testRegisterRetryTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        String bizId = RandomStringUtils.secure().nextNumeric(16);
        request.setBizType("ORDER");
        request.setBizId(bizId);
        request.setPriority(Priority.HIGH);

        Long taskId = retryTaskFacade.register(request);

        assertThat(taskId).isNotNull();
        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getBizType()).isEqualTo("ORDER");
        assertThat(task.getBizId()).isEqualTo(bizId);
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Should execute immediately and wait for result")
    void testRegisterWithImmediateExecutionAndWait() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("FACADE_TEST");
        String bizId = RandomStringUtils.secure().nextNumeric(16);
        request.setBizId(bizId);
        request.setExecuteImmediately(true);
        request.setWaitResult(true);

        Long taskId = retryTaskFacade.register(request);

        assertThat(taskId).isNotNull();
        // Task should be deleted after immediate execution
        assertThat(retryTaskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("Should execute immediately without waiting")
    void testRegisterWithImmediateExecutionNoWait() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("FACADE_TEST");
        String bizId = RandomStringUtils.secure().nextNumeric(16);
        request.setBizId(bizId);
        request.setExecuteImmediately(true);
        request.setWaitResult(false);

        Long taskId = retryTaskFacade.register(request);

        assertThat(taskId).isNotNull();

        // Task should eventually be deleted after async execution
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(retryTaskRepository.findById(taskId)).isEmpty();
        });
    }

    @Test
    @DisplayName("Should register without immediate execution")
    void testRegisterWithoutImmediateExecution() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("FACADE_TEST");
        String bizId = RandomStringUtils.secure().nextNumeric(16);
        request.setBizId(bizId);
        request.setExecuteImmediately(false);

        Long taskId = retryTaskFacade.register(request);

        assertThat(taskId).isNotNull();
        // Task should still exist
        assertThat(retryTaskRepository.findById(taskId)).isPresent();
    }

    @Test
    @DisplayName("Should register with priority from request")
    void testRegisterWithPriority() {
        RetryTaskRegisterRequest highPriorityRequest = new RetryTaskRegisterRequest();
        highPriorityRequest.setBizType("PRIORITY_TEST");
        highPriorityRequest.setBizId("HIGH-001");
        highPriorityRequest.setPriority(Priority.HIGH);

        Long highTaskId = retryTaskFacade.register(highPriorityRequest);

        RetryTask highTask = retryTaskRepository.findById(highTaskId).orElseThrow();
        assertThat(highTask.getPriority()).isEqualTo(Priority.HIGH);

        RetryTaskRegisterRequest lowPriorityRequest = new RetryTaskRegisterRequest();
        lowPriorityRequest.setBizType("PRIORITY_TEST");
        lowPriorityRequest.setBizId("LOW-001");
        lowPriorityRequest.setPriority(Priority.LOW);

        Long lowTaskId = retryTaskFacade.register(lowPriorityRequest);

        RetryTask lowTask = retryTaskRepository.findById(lowTaskId).orElseThrow();
        assertThat(lowTask.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    @DisplayName("Should use default priority if not specified")
    void testRegisterWithDefaultPriority() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("DEFAULT_PRIORITY");
        String bizId = RandomStringUtils.secure().nextNumeric(16);
        request.setBizId(bizId);

        Long taskId = retryTaskFacade.register(request);

        RetryTask task = retryTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getPriority()).isNotNull();
    }
}
