package io.github.loadup.components.signature.exception;

/** Unchecked exception thrown by the signature and digest services. */
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

    /** Categorized signature errors. */
    public enum SignatureErrorCode {
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

        public String getMessage() {
            return message;
        }
    }

    public SignatureErrorCode getErrorCode() {
        return this.errorCode;
    }
}
