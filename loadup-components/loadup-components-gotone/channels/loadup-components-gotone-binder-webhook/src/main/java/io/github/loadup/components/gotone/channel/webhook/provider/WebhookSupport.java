package io.github.loadup.components.gotone.channel.webhook.provider;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Shared HTTP support for the webhook providers.
 */
final class WebhookSupport {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private WebhookSupport() {}

    /**
     * POSTs a JSON body to the given webhook URL.
     *
     * @param url the webhook URL
     * @param json the JSON payload
     * @return {@code true} when the endpoint answered with a 2xx status
     * @throws IOException when the call fails
     * @throws InterruptedException when the call is interrupted
     */
    static boolean postJson(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, UTF_8))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    static String configValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    static String maskUrl(String url) {
        if (url == null || url.length() < 50) {
            return "***";
        }
        return url.substring(0, 40) + "...";
    }
}
