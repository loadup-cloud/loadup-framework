package io.github.loadup.retrytask.test.strategy;

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

import io.github.loadup.retrytask.strategy.ExponentialBackoffRetryStrategy;
import io.github.loadup.retrytask.strategy.FixedRetryStrategy;
import io.github.loadup.retrytask.strategy.RetryStrategy;
import io.github.loadup.retrytask.strategy.RetryStrategyRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RetryStrategyRegistry
 */
@ExtendWith(MockitoExtension.class)
class RetryStrategyRegistryTest {

    @Mock
    private RetryStrategy customStrategy;

    private RetryStrategyRegistry registry;

    @BeforeEach
    void setUp() {
        // Given
        when(customStrategy.getType()).thenReturn("custom");
        List<RetryStrategy> strategies =
                Arrays.asList(new FixedRetryStrategy(), new ExponentialBackoffRetryStrategy(), customStrategy);
        registry = new RetryStrategyRegistry(strategies);
    }

    @Test
    @DisplayName("Should register and retrieve built-in and custom strategies")
    void testRegisterAndRetrieveStrategies() {
        // When
        RetryStrategy fixed = registry.getStrategy("fixed");
        RetryStrategy exponential = registry.getStrategy("exponential");
        RetryStrategy custom = registry.getStrategy("custom");

        // Then
        assertNotNull(fixed);
        assertTrue(fixed instanceof FixedRetryStrategy);

        assertNotNull(exponential);
        assertTrue(exponential instanceof ExponentialBackoffRetryStrategy);

        assertNotNull(custom);
        assertEquals(customStrategy, custom);
        verify(customStrategy, atLeastOnce()).getType();
    }

    @Test
    @DisplayName("Should return null for an unknown strategy type")
    void testGetUnknownStrategy() {
        // When
        RetryStrategy unknown = registry.getStrategy("unknown");

        // Then
        assertNull(unknown);
    }

    @Test
    @DisplayName("Should handle initialization with an empty list")
    void testEmptyRegistryInitialization() {
        // Given
        RetryStrategyRegistry emptyRegistry = new RetryStrategyRegistry(Collections.emptyList());

        // When
        RetryStrategy strategy = emptyRegistry.getStrategy("fixed");

        // Then
        assertNull(strategy);
    }
}
