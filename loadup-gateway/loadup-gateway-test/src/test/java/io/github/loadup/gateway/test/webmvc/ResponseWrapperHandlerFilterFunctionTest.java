package io.github.loadup.gateway.test.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.webmvc.filter.ResponseWrapperHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.loadup.gateway.webmvc.support.RouteConfigConverter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@DisplayName("ResponseWrapperHandlerFilterFunction")
class ResponseWrapperHandlerFilterFunctionTest {

    @Test
    @DisplayName("wraps a successful backend body in the result/data/meta envelope")
    void wrapsSuccessBody() throws Exception {
        GatewayProperties properties = new GatewayProperties();
        properties.getResponse().setWrap(true);
        ResponseWrapperHandlerFilterFunction filter = new ResponseWrapperHandlerFilterFunction(properties);

        RouteConfig route = RouteConfigConverter.convert(beanRoute(), properties);
        ServerRequest request = WebMvcRequests.request("POST", "/api/demo", "{}");
        MvcUtils.putAttribute(request, GatewayAttributes.ROUTE_CONFIG, route);
        MvcUtils.putAttribute(
                request,
                GatewayAttributes.PROXY_RESPONSE,
                GatewayResponse.builder()
                        .requestId("req-1")
                        .statusCode(200)
                        .body("{\"message\":\"hello\"}")
                        .build());

        HandlerFunction<ServerResponse> next = serverRequest -> ServerResponse.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"message\":\"hello\"}");

        ServerResponse wrapped = filter.filter(request, next);

        assertThat(wrapped.statusCode().value()).isEqualTo(200);
        MockHttpServletResponse servletResponse = writeTo(request, wrapped);
        String body = servletResponse.getContentAsString();
        assertThat(body).contains("\"result\"");
        assertThat(body).contains("\"data\"");
        assertThat(body).contains("\"meta\"");
        assertThat(body).contains("\"message\":\"hello\"");
    }

    @Test
    @DisplayName("does not wrap error responses")
    void skipsErrorResponses() throws Exception {
        GatewayProperties properties = new GatewayProperties();
        properties.getResponse().setWrap(true);
        ResponseWrapperHandlerFilterFunction filter = new ResponseWrapperHandlerFilterFunction(properties);

        RouteConfig route = RouteConfigConverter.convert(beanRoute(), properties);
        ServerRequest request = WebMvcRequests.request("POST", "/api/demo", "{}");
        MvcUtils.putAttribute(request, GatewayAttributes.ROUTE_CONFIG, route);
        MvcUtils.putAttribute(
                request,
                GatewayAttributes.PROXY_RESPONSE,
                GatewayResponse.builder()
                        .requestId("req-1")
                        .statusCode(500)
                        .body("boom")
                        .build());

        HandlerFunction<ServerResponse> next =
                serverRequest -> ServerResponse.status(500).body("boom");

        ServerResponse wrapped = filter.filter(request, next);
        assertThat(wrapped.statusCode().value()).isEqualTo(500);
        MockHttpServletResponse servletResponse = writeTo(request, wrapped);
        assertThat(servletResponse.getContentAsString()).isEqualTo("boom");
    }

    private static MockHttpServletResponse writeTo(ServerRequest request, ServerResponse response) throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        List<HttpMessageConverter<?>> converters = List.of(new StringHttpMessageConverter());
        response.writeTo(request.servletRequest(), servletResponse, () -> converters);
        return servletResponse;
    }

    private static RouteDefinition beanRoute() {
        RouteDefinition def = new RouteDefinition();
        def.setId("demo");
        def.setPath("/api/demo");
        def.setMethod("POST");
        BackendDefinition backend = new BackendDefinition();
        backend.setProtocol("bean");
        backend.setBeanName("demoService");
        backend.setMethodName("hello");
        def.setBackend(backend);
        return def;
    }
}
