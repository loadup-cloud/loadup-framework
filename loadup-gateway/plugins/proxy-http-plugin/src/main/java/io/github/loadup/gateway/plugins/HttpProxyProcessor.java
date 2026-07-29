package io.github.loadup.gateway.plugins;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HttpProxyProcessor implements ProxyProcessor {
    private static final Logger log = LoggerFactory.getLogger(HttpProxyProcessor.class);


    private final RestClient restClient;

    public HttpProxyProcessor(GatewayProperties props) {
        this.restClient = RestClient.create();
        log.info("HttpProxyProcessor initialized with default RestClient");
    }

    @Override public String getName() { return "HttpProxyPlugin"; }
    @Override public String getType() { return "PROXY"; }
    @Override public String getVersion() { return "2.0.0"; }
    @Override public int getPriority() { return 200; }
    @Override public void initialize() {}
    @Override public void destroy() {}
    @Override public String getSupportedProtocol() { return GatewayConstants.Protocol.HTTP; }

    @Override
    public GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception {
        String target = route.getTargetUrl();
        String fullUrl = buildFullUrl(target, request);
        URI uri = URI.create(fullUrl);
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

        log.debug("Proxying {} → {}", method, fullUrl);

        ResponseEntity<String> response;
        if (method == HttpMethod.GET) {
            response = restClient.get().uri(uri).retrieve().toEntity(String.class);
        } else if (method == HttpMethod.PUT) {
            response = restClient.put().uri(uri).body(request.getBody()).retrieve().toEntity(String.class);
        } else if (method == HttpMethod.DELETE) {
            response = restClient.delete().uri(uri).retrieve().toEntity(String.class);
        } else if (method == HttpMethod.PATCH) {
            response = restClient.patch().uri(uri).body(request.getBody()).retrieve().toEntity(String.class);
        } else {
            response = restClient.post().uri(uri).body(request.getBody()).retrieve().toEntity(String.class);
        }

        Map<String, String> responseHeaders = new HashMap<>();
        response.getHeaders().forEach((k, values) -> {
            if (!values.isEmpty()) responseHeaders.put(k, values.get(0));
        });

        return GatewayResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(response.getStatusCode().value())
                .headers(responseHeaders)
                .body(response.getBody())
                .contentType(responseHeaders.get("Content-Type"))
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String buildFullUrl(String target, GatewayRequest request) {
        StringBuilder url = new StringBuilder(target);
        Map<String, List<String>> params = request.getQueryParameters();
        if (params != null && !params.isEmpty()) {
            url.append(target.contains("?") ? "&" : "?");
            boolean first = true;
            for (var entry : params.entrySet()) {
                for (String value : entry.getValue()) {
                    if (!first) url.append("&");
                    url.append(entry.getKey()).append("=").append(value);
                    first = false;
                }
            }
        }
        return url.toString();
    }
}
