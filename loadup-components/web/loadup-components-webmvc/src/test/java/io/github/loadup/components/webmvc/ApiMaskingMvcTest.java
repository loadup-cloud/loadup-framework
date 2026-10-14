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
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
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

        @PostMapping("/api/page")
        public PageDTO<String> page() { return PageDTO.of(List.of("one"), 21L, 2, 20); }

        @PostMapping("/api/null")
        public Object empty() { return null; }

        @PostMapping("/api/failure")
        public FailureResponse<Void> failure() { return FailureResponse.of(CommonResultCodeEnum.CONFLICT); }

        @PostMapping(value = "/api/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public byte[] binary() { return new byte[] {1, 2, 3}; }

        @PostMapping(value = "/api/resource", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ByteArrayResource resource() { return new ByteArrayResource(new byte[] {4, 5}); }

        @PostMapping(value = "/api/vendor", produces = "application/vnd.loadup+json")
        public Contact vendor() { return contact(); }

        @PostMapping("/protocol/contact")
        public Contact protocol() { return contact(); }

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
    @Test
    void paginationNullAndFailureEnvelopesUseTopLevelContractAndOutcomeMetrics() throws Exception {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        var registry = new SimpleMeterRegistry();
        try {
            var mvc = MockMvcBuilders.standaloneSetup(new Controller())
                    .setControllerAdvice(new ApiResponseAdvice(new ApiPathMatcher(), masking, new ApiResultMetrics(registry)))
                    .setMessageConverters(new JacksonJsonHttpMessageConverter(masking.mapper())).build();
            var page = mvc.perform(post("/api/page").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
            var json = JsonMapper.builder().build().readTree(page.getContentAsString());
            assertThat(json.path("data").isArray()).isTrue();
            assertThat(json.path("data").get(0).asString()).isEqualTo("one");
            assertThat(json.path("pageInfo").path("pageIndex").asInt()).isEqualTo(2);
            assertThat(json.path("pageInfo").path("totalCount").asLong()).isEqualTo(21);
            var empty = mvc.perform(post("/api/null").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
            assertThat(empty.getContentAsString()).contains("\"data\":null", "\"result\"");
            var failed = mvc.perform(post("/api/failure").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
            assertThat(failed.getStatus()).isEqualTo(200);
            assertThat(failed.getContentAsString()).contains("CONFLICT").doesNotContain("pageInfo");
            assertThat(registry.get("loadup.api.responses").tag("outcome", "success").counter().count()).isEqualTo(2);
            assertThat(registry.get("loadup.api.responses").tag("outcome", "failure").counter().count()).isEqualTo(1);
        } finally { registry.close(); }
    }

    @Test
    void downloadsAndProtocolResponsesPreserveTheirOwnRepresentation() throws Exception {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        var mvc = MockMvcBuilders.standaloneSetup(new Controller())
                .setControllerAdvice(new ApiResponseAdvice(new ApiPathMatcher(), masking, mock(ApiResultMetrics.class)))
                .setMessageConverters(new ByteArrayHttpMessageConverter(), new ResourceHttpMessageConverter(),
                        new JacksonJsonHttpMessageConverter(masking.mapper())).build();
        assertThat(mvc.perform(post("/api/binary")).andReturn().getResponse().getContentAsByteArray())
                .isEqualTo(new byte[] {1, 2, 3});
        assertThat(mvc.perform(post("/api/resource")).andReturn().getResponse().getContentAsByteArray())
                .isEqualTo(new byte[] {4, 5});
        var protocol = mvc.perform(post("/protocol/contact").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        assertThat(protocol.getContentAsString()).contains("phone").doesNotContain("\"data\"", "\"result\"");
        var vendor = mvc.perform(post("/api/vendor").accept("application/vnd.loadup+json")).andReturn().getResponse();
        assertThat(vendor.getContentAsString()).contains("\"result\"", "138****5678");
    }

    @Test
    void springAssemblyInstallsSharedJsonConvertersAndExecutionFilter() {
        var registry = new SimpleMeterRegistry();
        try {
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class,
                            HttpMessageConvertersAutoConfiguration.class, LoadUpWebMvcAutoConfiguration.class,
                            WebMvcAutoConfiguration.class))
                    .withBean(ErrorAttributes.class, () -> mock(ErrorAttributes.class))
                    .withBean(ApiResultMetrics.class, () -> new ApiResultMetrics(registry))
                    .withBean(Controller.class, Controller::new)
                    .run(application -> {
                        assertThat(application).hasNotFailed().hasSingleBean(ApiResponseAdvice.class)
                                .hasSingleBean(ApiExceptionHandler.class).hasSingleBean(ApiErrorController.class);
                        var filter = application.getBean("loadUpExecutionContextFilter", FilterRegistrationBean.class);
                        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
                        assertThat(filter.isAsyncSupported()).isTrue();
                        var mvc = MockMvcBuilders.webAppContextSetup(application.getSourceApplicationContext()).build();
                        var response = mvc.perform(post("/api/contact").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
                        assertThat(response.getContentAsString()).contains("138****5678", "\"result\"");
                    });
        } finally { registry.close(); }
    }

}
