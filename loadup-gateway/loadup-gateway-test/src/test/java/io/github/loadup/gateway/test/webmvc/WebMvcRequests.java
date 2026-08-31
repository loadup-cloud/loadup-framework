/*-
 * #%L
 * LoadUp Gateway Test
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
