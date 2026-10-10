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
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/** Transport headers with strict duplicate rejection and canonical Base64 encoding. */
public record RequestSignature(RequestSignatureParameters parameters, String signature) {
    public RequestSignature {
        if (parameters == null || signature == null || signature.length() > 684) throw malformed();
        try {
            byte[] bytes = Base64.getDecoder().decode(signature);
            if ((bytes.length != 256 && bytes.length != 384 && bytes.length != 512)
                    || !Base64.getEncoder().encodeToString(bytes).equals(signature)) throw malformed();
        } catch (IllegalArgumentException invalid) {
            throw malformed();
        }
    }

    public Map<String, String> toHeaders() {
        return Map.of(
                "X-Signature-Protocol",
                RequestSignatureCanonicalizer.PROTOCOL,
                "X-App-Id",
                parameters.appId(),
                "X-Signature-Audience",
                parameters.audience(),
                "X-Key-Version",
                Integer.toString(parameters.keyVersion()),
                "X-Signature-Algorithm",
                parameters.algorithm().name(),
                "X-Timestamp",
                Long.toString(parameters.timestamp()),
                "X-Nonce",
                parameters.nonce(),
                "X-Signature",
                signature);
    }

    public static RequestSignature fromHeaders(Map<String, ? extends List<String>> headers) {
        if (headers == null) throw malformed();
        if (!RequestSignatureCanonicalizer.PROTOCOL.equals(single(headers, "X-Signature-Protocol"))) throw malformed();
        String version = single(headers, "X-Key-Version");
        String timestamp = single(headers, "X-Timestamp");
        if (!version.matches("[1-9][0-9]{0,9}") || !timestamp.matches("0|[1-9][0-9]{0,17}")) throw malformed();
        try {
            return new RequestSignature(
                    new RequestSignatureParameters(
                            single(headers, "X-App-Id"),
                            single(headers, "X-Signature-Audience"),
                            Integer.parseInt(version),
                            KmsSignatureAlgorithm.valueOf(single(headers, "X-Signature-Algorithm")),
                            Long.parseLong(timestamp),
                            single(headers, "X-Nonce")),
                    single(headers, "X-Signature"));
        } catch (IllegalArgumentException invalid) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
    }

    private static String single(Map<String, ? extends List<String>> headers, String name) {
        String result = null;
        for (var entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                var values = entry.getValue();
                if (result != null
                        || values == null
                        || values.size() != 1
                        || values.getFirst() == null
                        || values.getFirst().length() > 1024) throw malformed();
                result = values.getFirst();
            }
        }
        if (result == null) throw malformed();
        return result;
    }

    private static RequestSignatureException malformed() {
        return new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(
                java.util.Map.of("appId", parameters.appId(), "keyVersion", parameters.keyVersion()));
    }
}
