/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.FailureResponse;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

class ApiExceptionHandlerTest {
    record Input(int value) {
        Input {
            if (value < 0) throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
    }

    @RestController
    static class Controller {
        @PostMapping("/api/business")
        Object business() {
            throw new CommonException(CommonResultCodeEnum.CONFLICT, new IllegalStateException("private-secret"));
        }

        @PostMapping("/api/system-code")
        Object systemCode() {
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR);
        }

        @PostMapping("/api/not-found")
        Object notFound() {
            throw new CommonException(CommonResultCodeEnum.NOT_FOUND);
        }

        @PostMapping("/api/invalid")
        Object invalid() {
            throw new IllegalArgumentException("private-secret");
        }

        @PostMapping("/api/constraint")
        Object constraint() {
            throw new jakarta.validation.ConstraintViolationException(java.util.Set.of());
        }

        @PostMapping("/api/nested")
        Object nested() {
            throw new IllegalArgumentException("binding failed", new CommonException(CommonResultCodeEnum.CONFLICT));
        }

        @PostMapping("/api/unknown")
        Object unknown() {
            throw new IllegalStateException("private-secret");
        }

        @PostMapping("/api/input")
        Object input(@RequestBody Input input) {
            return input;
        }

        @PostMapping("/api/denied")
        Object denied() {
            throw new org.springframework.security.access.AccessDeniedException("denied");
        }

        @PostMapping("/api/unauthenticated")
        Object unauthenticated() {
            throw new org.springframework.security.authentication.BadCredentialsException("invalid");
        }

        @PostMapping("/protocol")
        Object protocol() {
            throw new CommonException(CommonResultCodeEnum.CONFLICT);
        }
    }

    @Test
    void preservesBusinessCodesAndHidesUnexpectedFailureDetails() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Controller())
                .setControllerAdvice(new ApiExceptionHandler(new ApiPathMatcher()))
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
        for (var entry : java.util.Map.of(
                        "/api/business", "CONFLICT", "/api/unknown", "SYS_ERROR", "/api/input", "PARAM_ILLEGAL")
                .entrySet()) {
            var response = mvc.perform(
                            post(entry.getKey()).contentType("application/json").content("{\"value\":-1}"))
                    .andReturn()
                    .getResponse();
            assertThat(response.getStatus()).isEqualTo(200);
            var decoded = JsonMapper.builder().build().readValue(response.getContentAsString(), FailureResponse.class);
            assertThat(decoded.getResult().getCode()).isEqualTo(entry.getValue());
            assertThat(response.getContentAsString())
                    .contains("\"result\"", "\"data\"", entry.getValue())
                    .doesNotContain("private-secret");
        }
        assertThatThrownBy(() -> mvc.perform(post("/api/denied")))
                .hasCauseInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        assertThatThrownBy(() -> mvc.perform(post("/api/unauthenticated")))
                .hasCauseInstanceOf(org.springframework.security.core.AuthenticationException.class);
        assertThatThrownBy(() -> mvc.perform(post("/protocol"))).hasCauseInstanceOf(CommonException.class);
    }

    @Test
    void malformedInputAndDeclaredFailuresRemainJsonAndPreserveStableCodes() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Controller())
                .setControllerAdvice(new ApiExceptionHandler(new ApiPathMatcher()))
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
        for (var entry : java.util.Map.of(
                        "/api/system-code",
                        "SYS_ERROR",
                        "/api/not-found",
                        "NOT_FOUND",
                        "/api/invalid",
                        "PARAM_ILLEGAL",
                        "/api/constraint",
                        "PARAM_ILLEGAL",
                        "/api/nested",
                        "CONFLICT")
                .entrySet()) {
            var response = mvc.perform(post(entry.getKey())).andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContentAsString())
                    .contains(entry.getValue(), "\"data\"")
                    .doesNotContain("private-secret");
        }
        var malformed = mvc.perform(
                        post("/api/input").contentType("application/json").content("{broken"))
                .andReturn()
                .getResponse();
        assertThat(malformed.getStatus()).isEqualTo(200);
        assertThat(malformed.getContentAsString()).contains("PARAM_ILLEGAL").doesNotContain("broken");
    }

    @Test
    void errorDispatchMapsApiStatusesButPreservesProtocolStatus() throws Exception {
        var attributes = mock(ErrorAttributes.class);
        when(attributes.getErrorAttributes(any(), any())).thenReturn(java.util.Map.of("error", "protocol-error"));
        var mvc = MockMvcBuilders.standaloneSetup(new ApiErrorController(new ApiPathMatcher(), attributes))
                .addPlaceholderValue("spring.web.error.path", "/error")
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
        for (var entry : java.util.Map.of(
                        400,
                        "PARAM_ILLEGAL",
                        405,
                        "PARAM_ILLEGAL",
                        415,
                        "PARAM_ILLEGAL",
                        401,
                        "UNAUTHENTICATED",
                        403,
                        "ACCESS_DENIED",
                        404,
                        "NOT_FOUND",
                        500,
                        "SYS_ERROR")
                .entrySet()) {
            var response = mvc.perform(post("/error")
                            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, entry.getKey())
                            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/missing"))
                    .andReturn()
                    .getResponse();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContentAsString()).contains(entry.getValue(), "\"result\"", "\"data\"");
        }
        var protocol = mvc.perform(post("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/protocol/missing"))
                .andReturn()
                .getResponse();
        assertThat(protocol.getStatus()).isEqualTo(404);
        assertThat(protocol.getContentAsString()).contains("protocol-error").doesNotContain("\"result\"");
        var noOriginalUri = mvc.perform(post("/error")).andReturn().getResponse();
        assertThat(noOriginalUri.getStatus()).isEqualTo(500);
    }

    @Test
    void errorDispatchHonorsContextPathAndLogsOnlyFailureType() throws Exception {
        var attributes = mock(ErrorAttributes.class);
        when(attributes.getError(any())).thenReturn(new IllegalStateException("private-secret"));
        var mvc = MockMvcBuilders.standaloneSetup(new ApiErrorController(new ApiPathMatcher(), attributes))
                .addPlaceholderValue("spring.web.error.path", "/error")
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
        var response = mvc.perform(post("/app/error")
                        .contextPath("/app")
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/app/api")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("SYS_ERROR").doesNotContain("private-secret");
    }
}
