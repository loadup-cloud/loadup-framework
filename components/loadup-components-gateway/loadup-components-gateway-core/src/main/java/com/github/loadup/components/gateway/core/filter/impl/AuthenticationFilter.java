package com.github.loadup.components.gateway.core.filter.impl;

import com.github.loadup.components.gateway.core.config.GatewayProperties;
import com.github.loadup.components.gateway.core.filter.GatewayFilter;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Authentication Filter
 * 认证过滤器，用于JWT token的验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final GatewayProperties gatewayProperties;

    @Override
    public int getOrder() {
        return -100; // 认证过滤器优先级较高
    }

    @Override
    public boolean isEnabled() {
        return gatewayProperties.getSecurity().isEnabled();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 检查是否是忽略的路径
        String path = request.getRequestURI();
        if (gatewayProperties.getSecurity().getIgnoredPaths().stream()
            .anyMatch(ignoredPath -> path.startsWith(ignoredPath))) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing authentication token");
            return false;
        }

        try {
            // 验证token
            token = token.replace("Bearer ", "");
            JWSObject jwsObject = JWSObject.parse(token);
            JWSVerifier verifier = new MACVerifier(gatewayProperties.getSecurity().getJwtSecret());

            if (!jwsObject.verify(verifier)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token signature");
                return false;
            }

            // 将用户信息添加到请求属性中
            request.setAttribute("user_claims", jwsObject.getPayload().toJSONObject());
            return true;

        } catch (Exception e) {
            log.error("Token verification failed", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return false;
        }
    }
}
