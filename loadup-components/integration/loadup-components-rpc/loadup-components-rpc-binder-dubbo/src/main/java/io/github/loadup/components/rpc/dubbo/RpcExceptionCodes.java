package io.github.loadup.components.rpc.dubbo;

import io.github.loadup.commons.error.CommonException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.dubbo.rpc.RpcException;

/**
 * Carries the provider's declared result code across the Dubbo invocation boundary.
 *
 * <p>Dubbo only guarantees the exception type and message. The provider encodes the enum code in
 * the message of a business {@link RpcException}; consumers must still map the value against their
 * own allowlist and treat unknown or missing codes as a dependency failure.
 */
public final class RpcExceptionCodes {
    private static final String MARKER = "loadup-result-code=";
    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{0,63}");
    private static final int MAX_CAUSE_DEPTH = 10;

    private RpcExceptionCodes() {}

    public static RpcException business(CommonException failure) {
        String code = failure.getResultCode() instanceof Enum<?>
                ? failure.getResultCode().getCode()
                : "";
        return new RpcException(RpcException.BIZ_EXCEPTION, MARKER + code);
    }

    /**
     * Returns the declared result code, or an empty string when the failure is not a business rejection.
     */
    public static String resultCode(Throwable failure) {
        Throwable current = failure;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof RpcException && current.getMessage() != null) {
                int index = current.getMessage().indexOf(MARKER);
                if (index >= 0) {
                    Matcher matcher = CODE.matcher(current.getMessage().substring(index + MARKER.length()));
                    if (matcher.lookingAt()) {
                        return matcher.group();
                    }
                }
            }
            if (current.getCause() == current) {
                return "";
            }
            current = current.getCause();
        }
        return "";
    }
}
