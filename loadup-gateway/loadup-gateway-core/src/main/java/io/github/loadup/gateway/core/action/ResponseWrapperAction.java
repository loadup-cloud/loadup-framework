package io.github.loadup.gateway.core.action;

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

import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.utils.JsonUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.Ordered;

/** Action to wrap response in standard format */
@Slf4j
public class ResponseWrapperAction implements GatewayAction {

    private final GatewayProperties gatewayProperties;

    public ResponseWrapperAction(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        chain.proceed(context);

        GatewayResponse response = context.getResponse();
        RouteConfig route = context.getRoute();

        boolean shouldWrap = false;
        if (response != null && route != null) {
            if (route.getWrapResponse() != null) {
                shouldWrap = route.getWrapResponse();
            } else if (gatewayProperties.getResponse() != null) {
                shouldWrap = gatewayProperties.getResponse().isWrap();
            }
        }

        if (shouldWrap) {
            try {
                // Assume response body is JSON, wrap it in Result
                Object data = null;
                if (response.getBody() != null) {
                    data = JsonUtil.fromJson(response.getBody(), Object.class);
                }
                if (data == null) {
                    data = response.getBody();
                }

                Map<String, Object> wrapper = new LinkedHashMap<>();

                // 1. Result block
                if (gatewayProperties.getResponse().isWrapResult()) {
                    // Use response status code or default to 200/success logic
                    wrapper.put("result", Result.buildSuccess());
                }

                // 2. Data block
                wrapper.put("data", data);

                // 3. Meta block
                if (gatewayProperties.getResponse().isWrapMeta()) {
                    ResultMeta meta = ResultMeta.of(RandomStringUtils.secure().nextNumeric(16));
                    if (context.getRequest() != null) {
                        meta = ResultMeta.of(
                                context.getRequest().getRequestId(),
                                context.getRequest().getRequestTime());
                    }
                    wrapper.put("meta", meta);
                }

                String newBody = JsonUtils.toJson(wrapper);

                response.setBody(newBody);
                if (response.getHeaders() == null) {
                    response.setHeaders(new HashMap<>());
                }
                response.getHeaders().put("Content-Type", "application/json");
                response.getHeaders()
                        .put("Content-Length", String.valueOf(newBody.getBytes(StandardCharsets.UTF_8).length));

            } catch (Exception e) {
                log.error("Failed to wrap response", e);
                // On error, we might leave the response as is or set error response
            }
        }
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 3000;
    }
}
