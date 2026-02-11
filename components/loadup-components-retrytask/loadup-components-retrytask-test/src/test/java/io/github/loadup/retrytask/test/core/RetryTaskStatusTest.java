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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
