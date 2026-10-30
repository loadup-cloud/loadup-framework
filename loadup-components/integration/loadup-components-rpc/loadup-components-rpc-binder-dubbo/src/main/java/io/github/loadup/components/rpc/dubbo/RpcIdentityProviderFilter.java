package io.github.loadup.components.rpc.dubbo;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.resourceserver.LoadUpJwtAuthenticationConverter;
import jakarta.validation.Validator;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Provider-side guard for every Dubbo-exposed facade.
 *
 * <p>It rebuilds the caller identity from the propagated access token, binds it to the invocation
 * thread and restores the previous context afterwards, so provider actor providers keep reading the
 * same credentials and authorities as on the HTTP path. Business rejections are rewritten into a
 * Dubbo exception that carries the declared result code; unknown failures stay opaque.
 */
@Activate(group = CommonConstants.PROVIDER, order = RpcIdentityProviderFilter.ORDER)
public final class RpcIdentityProviderFilter implements Filter {
    /**
     * Runs after the framework context filters but before the framework exception filter, so the
     * rewritten failure is the one serialized back to the caller.
     */
    public static final int ORDER = -20000;

    private final LoadUpJwtAuthenticationConverter converter = new LoadUpJwtAuthenticationConverter();
    private Validator validator;

    /** Injected by Dubbo from the Spring context when a {@link Validator} bean is available. */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        SecurityContext previous = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(authenticate(invocation));
            validate(invocation);
            Result result;
            try {
                result = invoker.invoke(invocation);
            } catch (CommonException failure) {
                throw RpcExceptionCodes.business(failure);
            }
            if (result.hasException() && result.getException() instanceof CommonException failure) {
                result.setException(RpcExceptionCodes.business(failure));
            }
            return result;
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private SecurityContext authenticate(Invocation invocation) {
        String authorization = invocation.getAttachment(RpcTokenContext.AUTHORIZATION);
        if (authorization == null) {
            authorization = RpcContext.getServerAttachment().getAttachment(RpcTokenContext.AUTHORIZATION);
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RpcException(RpcException.AUTHORIZATION_EXCEPTION, "Valid access token required");
        }
        var jwt = parse(authorization.substring(7));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(converter.convert(jwt));
        return context;
    }

    private static Jwt parse(String token) {
        try {
            return RpcJwtClaims.parse(token);
        } catch (RuntimeException invalid) {
            throw new RpcException(RpcException.AUTHORIZATION_EXCEPTION, "Valid access token required");
        }
    }

    private void validate(Invocation invocation) {
        if (validator == null) return;
        for (Object argument : invocation.getArguments()) {
            if (argument != null && !validator.validate(argument).isEmpty()) {
                throw new RpcException(RpcException.VALIDATION_EXCEPTION, "Invalid RPC command");
            }
        }
    }
}
