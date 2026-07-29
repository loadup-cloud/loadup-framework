package io.github.loadup.gateway.core.filter;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request body parser filter — normalizes request parameters into a unified
 * parsed representation for downstream filters and proxy processors.
 *
 * <p>Handles:
 * <ul>
 *   <li>{@code application/json} → parsed as Map via Jackson</li>
 *   <li>{@code application/x-www-form-urlencoded} → parsed as Map</li>
 *   <li>Query parameters → merged into parsed params</li>
 * </ul>
 *
 * <p>Parsed result is stored in {@code request.attributes["parsedBody"]}.
 */
public class BodyParserFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(BodyParserFilter.class);

    @Override
    public String name() {
        return "body-parser";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        GatewayRequest request = context.getRequest();
        Map<String, Object> parsed = new HashMap<>();

        // Parse query parameters
        if (request.getQueryParameters() != null) {
            request.getQueryParameters().forEach((k, values) -> {
                if (values != null && !values.isEmpty()) {
                    parsed.put(k, values.size() == 1 ? values.get(0) : values);
                }
            });
        }

        // Parse body based on content type
        String contentType = request.getContentType();
        String body = request.getBody();
        if (StringUtils.isNotBlank(body)) {
            if (contentType != null && contentType.contains("application/json")) {
                try {
                    Object jsonParsed = JsonUtil.fromJson(body, Object.class);
                    if (jsonParsed instanceof Map) {
                        parsed.putAll((Map<String, Object>) jsonParsed);
                    } else {
                        parsed.put("_raw", jsonParsed);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse JSON body, using raw string", e);
                    parsed.put("_raw", body);
                }
            } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                parseFormBody(body, parsed);
            } else {
                parsed.put("_raw", body);
            }
        }

        request.getAttributes().put("parsedBody", Collections.unmodifiableMap(parsed));
        chain.filter(context);
    }

    private void parseFormBody(String body, Map<String, Object> parsed) {
        try {
            String decoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
            for (String pair : decoded.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    parsed.put(kv[0], kv[1]);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse form body", e);
        }
    }
}
