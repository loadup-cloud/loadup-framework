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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import org.junit.jupiter.api.Test;
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
            throw new CommonException(CommonResultCodeEnum.CONFLICT);
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
}
