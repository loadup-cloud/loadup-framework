package io.github.loadup.gateway.test.webmvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * Test helper that builds a servlet {@link ServerRequest} around a {@link MockHttpServletRequest}.
 */
final class WebMvcRequests {

    private WebMvcRequests() {}

    static ServerRequest request(String method, String path, String body) {
        MockHttpServletRequest mock = new MockHttpServletRequest(method, path);
        if (body != null) {
            mock.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mock.setContent(body.getBytes(StandardCharsets.UTF_8));
        }
        return ServerRequest.create(mock, List.<HttpMessageConverter<?>>of());
    }
}
