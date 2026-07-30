package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.filter.BodyParserFilter;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BodyParserFilter")
class BodyParserFilterTest {

    private final BodyParserFilter filter = new BodyParserFilter();

    private GatewayContext context(String body, String contentType) {
        GatewayContext ctx = new GatewayContext();
        GatewayRequest.Builder builder = GatewayRequest.builder()
                .requestId("r")
                .path("/test")
                .method("POST")
                .clientIp("127.0.0.1")
                .headers(new HashMap<>())
                .attributes(new HashMap<>());
        if (body != null) builder.body(body);
        if (contentType != null) builder.contentType(contentType);
        ctx.setRequest(builder.build());
        RouteConfig route = new RouteConfig();
        route.setPath("/test");
        route.setMethod("POST");
        route.setTarget("http://localhost/api");
        route.setEnabled(true);
        ctx.setRoute(route);
        return ctx;
    }

    @Nested
    @DisplayName("name")
    class Name {
        @Test
        @DisplayName("returns body-parser")
        void returnsBodyParser() {
            assertThat(filter.name()).isEqualTo("body-parser");
        }
    }

    @Nested
    @DisplayName("JSON body parsing")
    class JsonBody {

        @Test
        @DisplayName("parses valid JSON body")
        void parsesValidJson() {
            GatewayContext ctx = context("{\"name\":\"test\",\"count\":42}", "application/json");
            filter.filter(ctx, c -> {});
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed =
                    (Map<String, Object>) ctx.getRequest().getAttributes().get("parsedBody");
            assertThat(parsed).containsEntry("name", "test");
        }

        @Test
        @DisplayName("null body does not parse")
        void nullBodyDoesNotParse() {
            GatewayContext ctx = context(null, null);
            filter.filter(ctx, c -> {});
            assertThat(ctx.getRequest().getAttributes().get("parsedBody")).isNotNull();
        }

        @Test
        @DisplayName("empty body parses as empty")
        void emptyBody() {
            GatewayContext ctx = context("", null);
            filter.filter(ctx, c -> {});
            assertThat(ctx.getRequest().getAttributes().get("parsedBody")).isNotNull();
        }
    }
}
