package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RetryTaskRegisterRequest
 */
class RetryTaskRegisterRequestTest {

    private RetryTaskRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RetryTaskRegisterRequest();
    }

    @Test
    @DisplayName("Should set and get bizType")
    void testBizType() {
        // Given
        String bizType = "ORDER";

        // When
        request.setBizType(bizType);

        // Then
        assertEquals(bizType, request.getBizType());
    }

    @Test
    @DisplayName("Should set and get bizId")
    void testBizId() {
        // Given
        String bizId = "12345";

        // When
        request.setBizId(bizId);

        // Then
        assertEquals(bizId, request.getBizId());
    }

    @Test
    @DisplayName("Should set and get args")
    void testArgs() {
        // Given
        Map<String, String> args = new HashMap<>();
        args.put("key1", "value1");
        args.put("key2", "value2");

        // When
        request.setArgs(args);

        // Then
        assertEquals(args, request.getArgs());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        // Then
        assertNull(request.getBizType());
        assertNull(request.getBizId());
        assertNull(request.getArgs());
    }
}
