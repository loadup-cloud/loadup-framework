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

import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
