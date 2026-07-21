package io.github.loadup.components.signature.exception;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;

/**
 * 签名异常
 *
 * @author loadup
 */
@Getter
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
    @Getter
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
    }
}
