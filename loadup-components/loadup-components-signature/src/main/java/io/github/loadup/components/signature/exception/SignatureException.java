package io.github.loadup.components.signature.exception;

/*-
 * #%L
 * LoadUp Components :: Signature
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

/**
 * 签名异常
 *
 * @author loadup
 */
public class SignatureException extends RuntimeException {

    private final SignatureErrorCode errorCode;

    public SignatureException(SignatureErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SignatureException(SignatureErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public SignatureException(SignatureErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SignatureException(SignatureErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 签名错误码
     */
    public enum SignatureErrorCode {
        INVALID_KEY("无效的密钥"),
        INVALID_ALGORITHM("不支持的算法"),
        SIGN_FAILED("签名失败"),
        VERIFY_FAILED("验签失败"),
        DIGEST_FAILED("摘要计算失败"),
        KEY_GENERATION_FAILED("密钥生成失败"),
        ENCODING_FAILED("编码转换失败");

        private final String message;

        SignatureErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public SignatureErrorCode getErrorCode() {
        return this.errorCode;
    }
}
