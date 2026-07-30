package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TemplateEngine")
class TemplateEngineTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    @Nested
    @DisplayName("request template processing")
    class RequestTemplate {

        @Test
        @DisplayName("simple request template modifies headers")
        void simpleTemplateModifiesHeaders() {
            GatewayRequest req = request();
            GatewayRequest result = engine.processRequestTemplate(req, "request.headers.put('X-Test', 'hello')");
            assertThat(result.getHeaders()).containsEntry("X-Test", "hello");
        }

        @Test
        @DisplayName("request template modifies body")
        void templateModifiesBody() {
            GatewayRequest req = requestWithBody("old");
            GatewayRequest result = engine.processRequestTemplate(req, "request.body = 'new'");
            assertThat(result.getBody()).isEqualTo("new");
        }

        @Test
        @DisplayName("null template returns original request")
        void nullTemplateReturnsOriginal() {
            GatewayRequest req = request();
            GatewayRequest result = engine.processRequestTemplate(req, null);
            assertThat(result).isSameAs(req);
        }

        @Test
        @DisplayName("empty template returns original request")
        void emptyTemplateReturnsOriginal() {
            GatewayRequest req = request();
            GatewayRequest result = engine.processRequestTemplate(req, "");
            assertThat(result).isSameAs(req);
        }

        @Test
        @DisplayName("blank template returns original request")
        void blankTemplateReturnsOriginal() {
            GatewayRequest req = request();
            GatewayRequest result = engine.processRequestTemplate(req, "   ");
            assertThat(result).isSameAs(req);
        }

        @Test
        @DisplayName("failing template returns original request")
        void failingTemplateReturnsOriginal() {
            GatewayRequest req = request();
            GatewayRequest result = engine.processRequestTemplate(req, "throw new RuntimeException('fail')");
            assertThat(result).isSameAs(req);
        }
    }

    @Nested
    @DisplayName("response template processing")
    class ResponseTemplate {

        @Test
        @DisplayName("simple response template modifies body")
        void simpleTemplateModifiesBody() {
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("r1")
                    .statusCode(200)
                    .body("old")
                    .build();
            GatewayResponse result = engine.processResponseTemplate(resp, "response.body = 'new'");
            assertThat(result.getBody()).isEqualTo("new");
        }

        @Test
        @DisplayName("null template returns original response")
        void nullTemplateReturnsOriginal() {
            GatewayResponse resp =
                    GatewayResponse.builder().requestId("r1").statusCode(200).build();
            GatewayResponse result = engine.processResponseTemplate(resp, null);
            assertThat(result).isSameAs(resp);
        }

        @Test
        @DisplayName("template can modify status code")
        void templateModifiesStatusCode() {
            GatewayResponse resp =
                    GatewayResponse.builder().requestId("r1").statusCode(200).build();
            GatewayResponse result = engine.processResponseTemplate(resp, "response.statusCode = 201");
            assertThat(result.getStatusCode()).isEqualTo(201);
        }
    }

    private GatewayRequest request() {
        return GatewayRequest.builder()
                .requestId("r1")
                .path("/test")
                .method("GET")
                .clientIp("127.0.0.1")
                .headers(new java.util.HashMap<>())
                .attributes(new java.util.HashMap<>())
                .build();
    }

    private GatewayRequest requestWithBody(String body) {
        return GatewayRequest.builder()
                .requestId("r1")
                .path("/test")
                .method("POST")
                .clientIp("127.0.0.1")
                .headers(new java.util.HashMap<>())
                .attributes(new java.util.HashMap<>())
                .body(body)
                .build();
    }
}
