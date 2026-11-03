/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.rpc.dubbo;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;

public final class RpcTokenContext {
    public static final String AUTHORIZATION = "loadup-authorization";

    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");

    private RpcTokenContext() {}

    public static <T> T call(String token, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        if (token == null || !TOKEN.matcher(token).matches()) {
            throw new IllegalArgumentException("RPC bearer credential is required");
        }
        RpcContextAttachment attachment = RpcContext.getClientAttachment();
        attachment.setAttachment(AUTHORIZATION, "Bearer " + token);
        try {
            return action.get();
        } finally {
            attachment.removeAttachment(AUTHORIZATION);
        }
    }
}
