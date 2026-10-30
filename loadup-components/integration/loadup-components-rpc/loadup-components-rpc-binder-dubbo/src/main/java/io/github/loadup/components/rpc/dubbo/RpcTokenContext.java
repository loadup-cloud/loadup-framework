package io.github.loadup.components.rpc.dubbo;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;

/**
 * Binds the caller's access token to a single Dubbo invocation.
 *
 * <p>The token travels as a client attachment so the provider can rebuild the security context
 * without trusting a self-declared tenant, producer or principal. The attachment is always removed
 * after the call, so credentials are never cached in thread state.
 */
public final class RpcTokenContext {
    public static final String AUTHORIZATION = "loadup-authorization";

    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");

    private RpcTokenContext() {}

    public static <T> T call(String token, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        if (token == null || !TOKEN.matcher(token).matches())
            throw new IllegalArgumentException("RPC bearer credential is required");
        RpcContextAttachment attachment = RpcContext.getClientAttachment();
        attachment.setAttachment(AUTHORIZATION, "Bearer " + token);
        try {
            return action.get();
        } finally {
            attachment.removeAttachment(AUTHORIZATION);
        }
    }
}
