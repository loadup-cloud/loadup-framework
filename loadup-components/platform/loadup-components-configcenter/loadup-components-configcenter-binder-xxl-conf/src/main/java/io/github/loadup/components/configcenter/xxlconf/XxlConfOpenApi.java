/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.configcenter.xxlconf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class XxlConfOpenApi implements AutoCloseable {
    private final XxlConfConfigCenterProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final RestClient queries;
    private final RestClient monitoring;

    XxlConfOpenApi(XxlConfConfigCenterProperties properties) {
        this.properties = properties;
        queries = client(Duration.ofSeconds(5));
        monitoring = client(Duration.ofSeconds(40));
    }

    List<String> queryKeys() {
        var response = queries.post()
                .uri("/queryKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(application())
                .retrieve()
                .body(new ParameterizedTypeReference<Response<KeyData>>() {});
        return requireData(response).appnameKeyData().getOrDefault(properties.appName(), List.of());
    }

    Map<String, Value> queryValues(List<String> keys) {
        var response = queries.post()
                .uri("/queryData")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "env",
                        properties.env(),
                        "appnameKeyData",
                        Map.of(properties.appName(), keys),
                        "simpleQuery",
                        false))
                .retrieve()
                .body(new ParameterizedTypeReference<Response<ConfigurationData>>() {});
        return requireData(response).confData().getOrDefault(properties.appName(), Map.of());
    }

    void monitor() {
        var response = monitoring
                .post()
                .uri("/monitor")
                .contentType(MediaType.APPLICATION_JSON)
                .body(application())
                .retrieve()
                .body(new ParameterizedTypeReference<Response<String>>() {});
        // XXL-Conf 2.2.1 signals a changed key with an application-level failure envelope.
        if (response == null
                || (response.code() != 200
                        && (response.code() != 500
                                || response.msg() == null
                                || !response.msg().startsWith("Monitor key update:")))) {
            throw new IllegalStateException("XXL-Conf rejected the monitor request");
        }
    }

    private static <T> T requireData(Response<T> response) {
        if (response == null || response.code() != 200 || response.data() == null) {
            throw new IllegalStateException("XXL-Conf rejected the configuration query");
        }
        return response.data();
    }

    private Map<String, Object> application() {
        return Map.of("env", properties.env(), "appnameList", List.of(properties.appName()));
    }

    private RestClient client(Duration readTimeout) {
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        String address = properties.address().toString().replaceFirst("/+$", "");
        return RestClient.builder()
                .baseUrl(address + "/openapi/confdata")
                .defaultHeader("XXL_CONF_ACCESS_TOKEN", properties.accessToken())
                .requestFactory(factory)
                .build();
    }

    @Override
    public void close() {
        httpClient.shutdownNow();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Response<T>(int code, String msg, T data) {}

    private record KeyData(Map<String, List<String>> appnameKeyData) {}

    private record ConfigurationData(Map<String, Map<String, Value>> confData) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Value(String value) {}
}
