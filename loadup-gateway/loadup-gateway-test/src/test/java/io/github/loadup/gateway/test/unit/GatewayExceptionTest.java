/*-
 * #%L
 * LoadUp Gateway Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.facade.exception.ErrorCode;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GatewayException and ErrorCode")
class GatewayExceptionTest {

    @Nested
    @DisplayName("ErrorType")
    class ErrorTypeTest {

        @Test
        @DisplayName("all expected types exist")
        void allTypesExist() {
            assertThat(ErrorType.valueOf("ROUTING")).isNotNull();
            assertThat(ErrorType.valueOf("VALIDATION")).isNotNull();
            assertThat(ErrorType.valueOf("SECURITY")).isNotNull();
            assertThat(ErrorType.valueOf("RATE_LIMIT")).isNotNull();
            assertThat(ErrorType.valueOf("SYSTEM")).isNotNull();
            assertThat(ErrorType.valueOf("NETWORK")).isNotNull();
        }
    }

    @Nested
    @DisplayName("ErrorCode")
    class ErrorCodeTest {

        @Test
        @DisplayName("all expected codes exist")
        void allCodesExist() {
            assertThat(ErrorCode.valueOf("ROUTE_NOT_FOUND")).isNotNull();
            assertThat(ErrorCode.valueOf("SECURITY_FORBIDDEN")).isNotNull();
            assertThat(ErrorCode.valueOf("SYSTEM_ERROR")).isNotNull();
            assertThat(ErrorCode.valueOf("PLUGIN_NOT_FOUND")).isNotNull();
            assertThat(ErrorCode.valueOf("PROXY_TIMEOUT")).isNotNull();
        }

        @Test
        @DisplayName("each code has message")
        void eachCodeHasMessage() {
            for (ErrorCode code : ErrorCode.values()) {
                assertThat(code.getMessage()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("GatewayExceptionFactory")
    class FactoryTest {

        @Test
        @DisplayName("routeNotFound creates correct exception")
        void routeNotFound() {
            GatewayException ex = GatewayExceptionFactory.routeNotFound("/api/missing");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.ROUTING);
        }

        @Test
        @DisplayName("unauthorized creates correct exception")
        void unauthorized() {
            GatewayException ex = GatewayExceptionFactory.unauthorized("Invalid token");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.SECURITY);
            assertThat(ex.getMessage()).contains("Invalid token");
        }

        @Test
        @DisplayName("forbidden creates correct exception")
        void forbidden() {
            GatewayException ex = GatewayExceptionFactory.forbidden("No permission");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.AUTHORIZATION);
            assertThat(ex.getMessage()).contains("No permission");
        }

        @Test
        @DisplayName("systemError creates correct exception")
        void systemError() {
            GatewayException ex = GatewayExceptionFactory.systemError("Something went wrong");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.SYSTEM);
        }

        @Test
        @DisplayName("wrap maps RuntimeException to SYSTEM error")
        void wrapRuntimeException() {
            RuntimeException cause = new RuntimeException("boom");
            GatewayException ex = GatewayExceptionFactory.wrap(cause, "test-module");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.SYSTEM);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("GatewayException basics")
    class GatewayExceptionBasics {

        @Test
        @DisplayName("constructor sets fields")
        void constructorSetsFields() {
            GatewayException ex =
                    new GatewayException("TEST_CODE", ErrorType.VALIDATION, "TEST_MODULE", "test message");
            assertThat(ex.getErrorCode()).isEqualTo("TEST_CODE");
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.VALIDATION);
            assertThat(ex.getMessage()).isEqualTo("test message");
        }

        @Test
        @DisplayName("getErrorCode and getErrorType work")
        void gettersWork() {
            GatewayException ex = new GatewayException("TEST", ErrorType.ROUTING, "MOD", "msg");
            assertThat(ex.getErrorCode()).isNotNull();
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.ROUTING);
        }
    }
}
