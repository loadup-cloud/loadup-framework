package io.github.loadup.retrytask.test.notify;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.notify.RetryTaskNotifier;
import io.github.loadup.retrytask.notify.RetryTaskNotifierRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
            public void notify(RetryTask task) {

            }

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
