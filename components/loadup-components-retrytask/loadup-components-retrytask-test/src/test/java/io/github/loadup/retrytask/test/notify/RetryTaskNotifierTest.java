package io.github.loadup.retrytask.test.notify;

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
import static org.mockito.Mockito.*;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.notify.RetryTaskNotifier;
import io.github.loadup.retrytask.notify.RetryTaskNotifierRegistry;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RetryTaskNotifier and its registry
 */
@ExtendWith(MockitoExtension.class)
class RetryTaskNotifierTest {

    @Mock
    private RetryTaskNotifier webhookNotifier;

    private RetryTaskNotifier loggingNotifier;
    private RetryTaskNotifierRegistry registry;
    private RetryTask retryTask;

    @BeforeEach
    void setUp() {
        // Given
        loggingNotifier = new RetryTaskNotifier() {
            @Override
            public void notify(RetryTask task) {}

            @Override
            public String getType() {
                return "log";
            }
        };
        when(webhookNotifier.getType()).thenReturn("webhook");

        List<RetryTaskNotifier> notifiers = Arrays.asList(loggingNotifier, webhookNotifier);
        registry = new RetryTaskNotifierRegistry(notifiers);

        retryTask = new RetryTask();
        retryTask.setBizType("ORDER");
        retryTask.setBizId("12345");
    }

    @Test
    @DisplayName("Should register notifiers correctly")
    void testRegistryInitialization() {
        // When
        RetryTaskNotifier log = registry.getNotifier("log");
        RetryTaskNotifier webhook = registry.getNotifier("webhook");

        // Then
        assertNotNull(log);
        assertEquals("log", log.getType());
        assertNotNull(webhook);
        assertEquals("webhook", webhook.getType());
    }

    @Test
    @DisplayName("Should return null for unknown notifier type")
    void testGetUnknownNotifier() {
        // When
        RetryTaskNotifier unknown = registry.getNotifier("unknown");

        // Then
        assertNull(unknown);
    }

    @Test
    @DisplayName("LoggingNotifier should not throw an exception")
    void testLoggingNotifier() {
        // When & Then
        assertDoesNotThrow(() -> loggingNotifier.notify(retryTask));
    }

    @Test
    @DisplayName("Notifiers should handle null task gracefully")
    void testNotifyWithNullTask() {
        // When & Then
        assertDoesNotThrow(() -> loggingNotifier.notify(null));
        assertDoesNotThrow(() -> registry.getNotifier("webhook").notify(null));
        verify(webhookNotifier, times(1)).notify(null);
    }

    @Test
    @DisplayName("Registry should be able to retrieve and use a notifier")
    void testRetrieveAndUseNotifier() {
        // Given
        String webhookType = "webhook";

        // When
        RetryTaskNotifier notifier = registry.getNotifier(webhookType);
        assertNotNull(notifier);
        notifier.notify(retryTask);

        // Then
        verify(webhookNotifier, times(1)).notify(retryTask);
    }
}
