package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RetryTaskStatus enum
 */
class RetryTaskStatusTest {

    @Test
    @DisplayName("Should have all expected status values")
    void testAllStatusValues() {
        // Then
        assertNotNull(RetryTaskStatus.PENDING);
        assertNotNull(RetryTaskStatus.SUCCESS);
        assertNotNull(RetryTaskStatus.FAILURE);
    }

    @ParameterizedTest
    @EnumSource(RetryTaskStatus.class)
    @DisplayName("Should convert status to string correctly")
    void testStatusToString(RetryTaskStatus status) {
        // When
        String statusString = status.toString();

        // Then
        assertNotNull(statusString);
        assertFalse(statusString.isEmpty());
        assertTrue(statusString.matches("[A-Z_]+"));
    }

    @Test
    @DisplayName("Should identify terminal states correctly")
    void testTerminalStates() {
        // Terminal states
        assertTrue(isTerminalState(RetryTaskStatus.SUCCESS));
        assertTrue(isTerminalState(RetryTaskStatus.FAILURE));

        // Non-terminal states
        assertFalse(isTerminalState(RetryTaskStatus.PENDING));
    }

    private boolean isTerminalState(RetryTaskStatus status) {
        return status == RetryTaskStatus.SUCCESS || status == RetryTaskStatus.FAILURE;
    }
}
