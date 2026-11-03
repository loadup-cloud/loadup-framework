/*
 * #%L
 * LoadUp Components RPC Binder Dubbo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.components.rpc.dubbo;

import io.github.loadup.commons.error.CommonException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.dubbo.rpc.RpcException;

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
