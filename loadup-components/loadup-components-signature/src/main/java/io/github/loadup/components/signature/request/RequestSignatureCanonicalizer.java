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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** LoadUp request v1 uses fixed-order ASCII fields, LF separators and a trailing LF. */
public final class RequestSignatureCanonicalizer {
    public static final String PROTOCOL = "loadup-request-v1";

    private RequestSignatureCanonicalizer() {}

    public static byte[] canonicalize(RequestContent content, RequestSignatureParameters parameters) {
        if (content == null || parameters == null)
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        String canonical = PROTOCOL + "\n"
                + "app-id:" + parameters.appId() + "\n"
                + "audience:" + parameters.audience() + "\n"
                + "key-version:" + parameters.keyVersion() + "\n"
                + "algorithm:" + parameters.algorithm().name() + "\n"
                + "method:" + content.method() + "\n"
                + "path:" + content.path() + "\n"
                + "query:" + content.query() + "\n"
                + "content-type:" + content.contentType() + "\n"
                + "timestamp:" + parameters.timestamp() + "\n"
                + "nonce:" + parameters.nonce() + "\n"
                + "payload-sha256:" + sha256(content.payload()) + "\n";
        return canonical.getBytes(StandardCharsets.UTF_8);
    }

    static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable");
        }
    }
}
