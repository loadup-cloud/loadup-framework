package io.github.loadup.commons.error;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
