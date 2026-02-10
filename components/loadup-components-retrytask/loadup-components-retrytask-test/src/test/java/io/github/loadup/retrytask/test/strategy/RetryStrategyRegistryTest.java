package io.github.loadup.retrytask.test.strategy;

import io.github.loadup.retrytask.strategy.RetryStrategyRegistry;
import io.github.loadup.retrytask.strategy.RetryStrategy;
import io.github.loadup.retrytask.strategy.FixedRetryStrategy;
import io.github.loadup.retrytask.strategy.ExponentialBackoffRetryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        List<RetryStrategy> strategies = Arrays.asList(
                new FixedRetryStrategy(),
                new ExponentialBackoffRetryStrategy(),
                customStrategy
        );
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
