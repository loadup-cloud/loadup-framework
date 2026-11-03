package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.loadup.components.observability.ApiResultMetrics;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

class RequestBodyLimitAdviceTest {
    private final Controller controller = new Controller();

    private MockMvc mvc(int maxBytes, Integer reportedLength) {
        var json = JsonMapper.builder().build();
        var builder = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(
                        new RequestBodyLimitAdvice(new RequestBodyLimitProperties("/api/internal/", DataSize.ofBytes(maxBytes))),
                        new ApiExceptionHandler(new ApiPathMatcher()),
                        new ApiResponseAdvice(new ApiPathMatcher(), new ApiMaskingJson(json), mock(ApiResultMetrics.class)))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(json));
        if (reportedLength != null) {
            builder.addFilter((request, response, chain) -> chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {
                @Override
                public int getContentLength() {
                    return reportedLength;
                }

                @Override
                public long getContentLengthLong() {
                    return reportedLength;
                }
            }, response));
        }
        return builder.build();
    }

    @Test
    void exactUtf8ByteBoundaryIsAcceptedAndOneByteMoreIsRejectedBeforeBusinessInvocation() throws Exception {
        byte[] body = "{\"value\":\"é\"}".getBytes(StandardCharsets.UTF_8);
        var mvc = mvc(body.length, null);
        assertThat(mvc.perform(post("/api/internal/v1/accept").contentType("application/json").content(body))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(controller.calls).hasValue(1);
        var oversized = mvc.perform(post("/api/internal/v1/accept").contentType("application/json")
                .content("{\"value\":\"é\"} ".getBytes(StandardCharsets.UTF_8))).andReturn().getResponse();
        assertThat(oversized.getStatus()).isEqualTo(413);
        assertThat(oversized.getContentAsString()).contains("PARAM_ILLEGAL");
        assertThat(controller.calls).hasValue(1);
    }

    @Test
    void unknownOrFalseLengthAndTrailingWhitespaceCannotBypassTheLimit() throws Exception {
        for (int length : new int[] {-1, 1}) {
            var response = mvc(32, length).perform(post("/api/internal/v1/accept")
                    .contentType("application/json").content("{\"value\":\"private-secret\"}" + " ".repeat(64)))
                    .andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(413);
            assertThat(response.getContentAsString()).contains("PARAM_ILLEGAL").doesNotContain("private-secret");
        }
        assertThat(controller.calls).hasValue(0);
    }

    @Test
    void matchedRouteHonorsContextPathAndEncodedSpellingWhileOtherApiPathsKeepTheirOwnProtocol() throws Exception {
        var mvc = mvc(8, -1);
        for (String path : new String[] {"/app/api/internal/v1/accept", "/app/%61pi/internal/v1/accept"}) {
            var response = mvc.perform(post(URI.create(path)).contextPath("/app").contentType("application/json")
                    .content("{\"value\":\"private-secret\"}")).andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(413);
        }
        var external = mvc.perform(post("/api/external/accept").contentType("application/json")
                .content("{\"value\":\"external\"}")).andReturn().getResponse();
        assertThat(external.getStatus()).isEqualTo(200);
        assertThat(controller.calls).hasValue(1);
    }

    @Test
    void malformedSmallJsonKeepsTheExistingBusinessEnvelopeAndCannotInvokeTheController() throws Exception {
        var response = mvc(64, -1).perform(post("/api/internal/v1/accept").contentType("application/json")
                .content("{broken")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("PARAM_ILLEGAL").doesNotContain("broken");
        assertThat(controller.calls).hasValue(0);
    }

    @Test
    void sdkIsOptInAndRejectsInvalidLimitsAtStartup() {
        var context = new ApplicationContextRunner().withUserConfiguration(LoadUpWebMvcAutoConfiguration.RequestBodyLimits.class);
        context.run(application -> assertThat(application).doesNotHaveBean(RequestBodyLimitAdvice.class));
        context.withPropertyValues("loadup.webmvc.request-body.max-size=1MB").run(application -> {
            assertThat(application).hasSingleBean(RequestBodyLimitAdvice.class);
            assertThat(application.getBean(RequestBodyLimitProperties.class).pathPrefix()).isEqualTo("/api/internal/");
        });
        context.withPropertyValues("loadup.webmvc.request-body.max-size=0B")
                .run(application -> assertThat(application).hasFailed());
    }

    record Input(String value) {}

    @RestController
    static class Controller {
        final AtomicInteger calls = new AtomicInteger();

        @PostMapping({"/api/internal/v1/accept", "/api/external/accept"})
        Input accept(@RequestBody Input input) {
            calls.incrementAndGet();
            return input;
        }
    }
}
