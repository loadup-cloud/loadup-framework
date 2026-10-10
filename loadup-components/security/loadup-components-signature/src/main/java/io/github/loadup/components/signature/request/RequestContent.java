/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import io.github.loadup.commons.json.ToStringAsJson;
import java.net.URI;

/** Exact HTTP origin-form target and body bytes; no decoding or JSON reserialization. */
public record RequestContent(String method, String target, String contentType, byte[] payload) {
    public RequestContent {
        if (contentType != null && !contentType.matches("[\\x20-\\x7e]*")) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
        contentType = contentType == null ? "" : contentType.trim();
        if (method == null
                || !method.matches("[A-Z]{1,16}")
                || target == null
                || target.length() > 8192
                || !target.startsWith("/")
                || target.startsWith("//")
                || !target.matches("[\\x21-\\x7e]+")
                || contentType.length() > 256
                || !contentType.matches("[\\x20-\\x7e]*")
                || payload == null) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
        try {
            URI uri = URI.create(target);
            if (uri.isAbsolute() || uri.getRawAuthority() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException invalid) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
        payload = payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    int payloadSize() {
        return payload.length;
    }

    String path() {
        int index = target.indexOf('?');
        return index < 0 ? target : target.substring(0, index);
    }

    String query() {
        int index = target.indexOf('?');
        return index < 0 ? "" : target.substring(index);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(java.util.Map.of("method", method, "payloadBytes", payload.length));
    }
}
