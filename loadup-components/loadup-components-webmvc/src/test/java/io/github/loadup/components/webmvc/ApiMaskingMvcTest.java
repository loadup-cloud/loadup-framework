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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.observability.ApiResultMetrics;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

class ApiMaskingMvcTest {
    public record Contact(@Masked(MaskType.PHONE) String phone) {}

    @RestController
    static class Controller {
        @PostMapping("/api/contact")
        public Contact contact() {
            return new Contact("13812345678");
        }

        @PostMapping("/api/envelope")
        public SuccessResponse<List<Contact>> envelope() {
            return SuccessResponse.of(List.of(contact()));
        }

        @PostMapping("/api/text")
        public String text() {
            return "hello";
        }
    }

    @Test
    void masksPlainAndPreWrappedApiResponses() throws Exception {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        var advice = new ApiResponseAdvice(new ApiPathMatcher(), masking, mock(ApiResultMetrics.class));
        var mvc = MockMvcBuilders.standaloneSetup(new Controller())
                .setControllerAdvice(advice)
                .setMessageConverters(
                        new StringHttpMessageConverter(), new JacksonJsonHttpMessageConverter(masking.mapper()))
                .build();
        for (String path : List.of("/api/contact", "/api/envelope")) {
            var response = mvc.perform(post(path).accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContentAsString())
                    .contains("138****5678", "\"result\"", "\"data\"")
                    .doesNotContain("13812345678");
        }
        var text = mvc.perform(post("/api/text").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        assertThat(text.getContentAsString()).contains("\"data\":\"hello\"", "\"result\"");
    }
}
