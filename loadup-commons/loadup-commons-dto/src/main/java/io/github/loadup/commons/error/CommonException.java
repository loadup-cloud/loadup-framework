package io.github.loadup.commons.error;

import io.github.loadup.commons.result.ResultCode;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lise
 * @since 1.0.0
 */
public class CommonException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2713503013175560520L;

    private final ResultCode resultCode;

    public CommonException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, String msg, Throwable cause) {
        super(msg, cause);
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();
        if (resultCode != null) {
            map.put("code", escapeJson(resultCode.getCode()));
            map.put("message", escapeJson(resultCode.getMessage()));
        }
        map.put("extraMessage", escapeJson(getMessage()));

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append('"')
                    .append(entry.getKey())
                    .append("\":\"")
                    .append(entry.getValue() != null ? entry.getValue() : "")
                    .append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public ResultCode getResultCode() {
        return this.resultCode;
    }
}
