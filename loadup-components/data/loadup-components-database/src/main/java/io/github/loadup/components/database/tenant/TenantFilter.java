package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.components.database.config.DatabaseProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Binds the configured request tenant to immutable execution metadata. */
public class TenantFilter extends OncePerRequestFilter {
    private final DatabaseProperties.MultiTenant tenantProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = tenantProperties.isEnabled()
                ? resolveTenantId(request)
                : tenantProperties.getDefaultTenantId();
        String attribute = ExecutionContext.class.getName();
        ExecutionContext context =
                request.getAttribute(attribute) instanceof ExecutionContext saved ? saved : ContextHolder.current();
        if (!tenantProperties.isEnabled() || tenantId != null) context = context.with(ContextKeys.TENANT_ID, tenantId);
        request.setAttribute(attribute, context);
        try {
            ContextHolder.callWith(context, () -> {
                filterChain.doFilter(request, response);
                return null;
            });
        } catch (IOException | ServletException | RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new ServletException(failure);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private String resolveTenantId(HttpServletRequest request) {
        DatabaseProperties.Request requestProperties = tenantProperties.getRequest();
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

    public TenantFilter(DatabaseProperties.MultiTenant tenantProperties) {
        this.tenantProperties = tenantProperties;
    }
}
