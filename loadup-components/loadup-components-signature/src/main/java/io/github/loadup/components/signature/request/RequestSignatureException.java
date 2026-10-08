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

/** Safe request verification failure without payloads, headers or backend details. */
public final class RequestSignatureException extends RuntimeException {
    public enum Code {
        MALFORMED,
        IDENTITY_MISMATCH,
        EXPIRED,
        FUTURE_TIMESTAMP,
        INVALID_SIGNATURE,
        REPLAY,
        REPLAY_STORE_UNAVAILABLE,
        PAYLOAD_TOO_LARGE
    }

    private final Code code;

    public RequestSignatureException(Code code) {
        super("Request signature rejected: " + code);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
