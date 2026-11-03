/*
 * #%L
 * LoadUp Components WebMVC
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.observability.ApiResultMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import tools.jackson.databind.json.JsonMapper;

class ProtocolResponseTest {
    @Test
    void nativeTextJsonAndMethodResponsesKeepExactBodyStatusAndContentTypeWhileOrdinaryApiStillWraps()
            throws Exception {
        var json = JsonMapper.builder().build();
        var mvc = MockMvcBuilders.standaloneSetup(new Callback(), new Ordinary())
                .setControllerAdvice(
                        new ApiResponseAdvice(
                                new ApiPathMatcher(), new ApiMaskingJson(json), mock(ApiResultMetrics.class)),
                        new ApiExceptionHandler(new ApiPathMatcher()))
                .setMessageConverters(new StringHttpMessageConverter(), new JacksonJsonHttpMessageConverter(json))
                .build();
        var failure = mvc.perform(post("/api/native/text")).andReturn().getResponse();
        assertThat(failure.getStatus()).isEqualTo(400);
        assertThat(failure.getContentAsString()).isEqualTo("failure");
        assertThat(failure.getContentType()).startsWith("text/plain");
        var success = mvc.perform(post("/api/native/json")).andReturn().getResponse();
        assertThat(success.getStatus()).isEqualTo(200);
        assertThat(success.getContentAsString()).isEqualTo("{\"code\":\"SUCCESS\"}");
        var method = mvc.perform(post("/api/method")).andReturn().getResponse();
        assertThat(method.getStatus()).isEqualTo(409);
        assertThat(method.getContentAsString()).isEqualTo("retry-original");
        var ordinary = mvc.perform(post("/api/ordinary")).andReturn().getResponse();
        assertThat(ordinary.getContentAsString()).contains("\"result\"", "\"data\":\"value\"");
        assertThatThrownBy(() -> mvc.perform(post("/api/native/throws"))).hasCauseInstanceOf(CommonException.class);
    }

    @Test
    void protocolDecisionSurvivesReplacementOfTheMatchedHandlerDuringErrorDispatch() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/native/text");
        var paths = new ApiPathMatcher();
        var callback = new HandlerMethod(new Callback(), Callback.class.getDeclaredMethod("text"));
        var ordinary = new HandlerMethod(new Ordinary(), Ordinary.class.getDeclaredMethod("value"));
        assertThat(new ProtocolResponseInterceptor(paths).preHandle(request, new MockHttpServletResponse(), callback))
                .isTrue();
        request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, ordinary);
        assertThat(paths.matches(request, "/api/native/text")).isFalse();
        assertThat(paths.matches(new MockHttpServletRequest("POST", "/api/ordinary"), "/api/ordinary"))
                .isTrue();
    }

    @RestController
    @ProtocolResponse
    static class Callback {
        @PostMapping("/api/native/text")
        ResponseEntity<String> text() {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("failure");
        }

        @PostMapping("/api/native/json")
        ResponseEntity<String> json() {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"SUCCESS\"}");
        }

        @PostMapping("/api/native/throws")
        String failure() {
            throw new CommonException(CommonResultCodeEnum.CONFLICT);
        }
    }

    @RestController
    static class Ordinary {
        @PostMapping("/api/ordinary")
        String value() {
            return "value";
        }

        @PostMapping("/api/method")
        @ProtocolResponse
        ResponseEntity<String> protocol() {
            return ResponseEntity.status(409).contentType(MediaType.TEXT_PLAIN).body("retry-original");
        }
    }
}
