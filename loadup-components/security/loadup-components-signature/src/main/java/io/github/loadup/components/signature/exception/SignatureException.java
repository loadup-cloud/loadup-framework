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
package io.github.loadup.components.signature.exception;

import io.github.loadup.commons.error.CommonException;

/**
 * Unchecked exception thrown by the signature and digest services.
 */
public class SignatureException extends CommonException {

    private final SignatureErrorCode errorCode;

    public SignatureException(SignatureErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public SignatureException(SignatureErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
    }

    public SignatureErrorCode getErrorCode() {
        return this.errorCode;
    }

    /**
     * Categorized signature errors.
     */
    public enum SignatureErrorCode implements io.github.loadup.commons.result.ResultCode {
        INVALID_KEY("Invalid key"),
        INVALID_ALGORITHM("Unsupported algorithm"),
        SIGN_FAILED("Sign failed"),
        VERIFY_FAILED("Verify failed"),
        DIGEST_FAILED("Digest failed"),
        KEY_GENERATION_FAILED("Key pair generation failed"),
        ENCODING_FAILED("Encoding failed");

        private final String message;

        SignatureErrorCode(String message) {
            this.message = message;
        }

        public String getCode() {
            return "SIGNATURE_" + name();
        }

        public String getStatus() {
            return io.github.loadup.commons.enums.ResultStatusEnum.FAIL.getCode();
        }

        public String getMessage() {
            return message;
        }
    }
}
