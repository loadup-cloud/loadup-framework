package io.github.loadup.components.signature.exception;

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

