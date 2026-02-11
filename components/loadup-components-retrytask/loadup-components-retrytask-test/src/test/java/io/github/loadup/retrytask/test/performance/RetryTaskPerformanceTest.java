package io.github.loadup.retrytask.test.performance;

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
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.starter.RetryTaskAutoConfiguration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Performance tests for the retry task framework
 */
@SpringBootTest(classes = RetryTaskAutoConfiguration.class)
@EnabledIfSystemProperty(named = "test.performance", matches = "true")
class RetryTaskPerformanceTest {

    @Autowired
    private RetryTaskFacade retryTaskFacade;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {}

    @Test
    @DisplayName("Should handle high volume of retry task registrations")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testHighVolumeTaskRegistration() {
        // Given
        int numberOfTasks = 1000;

        // When
        long startTime = System.currentTimeMillis();

        IntStream.range(0, numberOfTasks).parallel().forEach(i -> {
            RetryTaskRegisterRequest request = createRequest("PERF-" + i);
            Long taskId = retryTaskFacade.register(request);
            assertNotNull(taskId);
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        assertTrue(duration < 30000, "Registration of 1000 tasks should complete within 30 seconds");
    }

    @Test
    @DisplayName("Should handle concurrent operations efficiently")
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testConcurrentOperations() {
        // Given
        int numberOfThreads = 10;
        int tasksPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfThreads];

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> {
                        for (int j = 0; j < tasksPerThread; j++) {
                            RetryTaskRegisterRequest request = createRequest("THREAD-" + threadId + "-TASK-" + j);
                            Long taskId = retryTaskFacade.register(request);
                            assertNotNull(taskId);
                        }
                    },
                    executor);
        }

        // Then
        assertDoesNotThrow(() -> CompletableFuture.allOf(futures).get(15, TimeUnit.SECONDS));
        executor.shutdown();
    }

    private RetryTaskRegisterRequest createRequest(String bizId) {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("PERFORMANCE_TEST");
        request.setBizId(bizId);
        return request;
    }
}
