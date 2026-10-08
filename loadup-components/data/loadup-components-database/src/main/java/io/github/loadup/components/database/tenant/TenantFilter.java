package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Binds the configured request tenant to {@link TenantUtil}. */
public class TenantFilter extends OncePerRequestFilter {
    private final DatabaseProperties.Request requestProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String previousTenantId = TenantUtil.getTenantId();
        String tenantId = resolveTenantId(request);
        try {
            if (tenantId != null) {
                TenantUtil.setTenantId(tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            if (previousTenantId == null) {
                TenantUtil.clear();
            } else {
                TenantUtil.setTenantId(previousTenantId);
            }
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String tenantId = StringUtils.hasText(requestProperties.getHeaderName())
                ? readValue(request.getHeader(requestProperties.getHeaderName()))
                : null;
        if (tenantId != null) {
            return tenantId;
        }
        if (StringUtils.hasText(requestProperties.getParameterName())) {
            tenantId = readValue(request.getParameter(requestProperties.getParameterName()));
            if (tenantId != null) {
                return tenantId;
            }
        }
        if (!requestProperties.isSubdomainEnabled()) {
            return null;
        }
        String serverName = request.getServerName();
        if (!StringUtils.hasText(serverName) || !serverName.contains(".")) {
            return null;
        }
        String subdomain = serverName.substring(0, serverName.indexOf('.'));
        boolean excluded =
                requestProperties.getExcludedSubdomains().stream().anyMatch(value -> value.equalsIgnoreCase(subdomain));
        return excluded ? null : readValue(subdomain);
    }

    private String readValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    public TenantFilter(DatabaseProperties.Request requestProperties) {
        this.requestProperties = requestProperties;
    }
}
