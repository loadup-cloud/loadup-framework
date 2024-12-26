package com.github.loadup.components.gateway.common.exception;

/**
 * Base transaction
 */
public class GatewayException extends RuntimeException {

    /**
     * 异常代码
     */
    private final ErrorCode resultCode;

    /**
     * 构造函数
     */
    public GatewayException(ErrorCode resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * 构造函数
     */
    public GatewayException(ErrorCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 构造函数
     */
    public GatewayException(ErrorCode resultCode, Throwable e) {
        super(e);
        this.resultCode = resultCode;
    }

    /**
     * 构造函数
     */
    public GatewayException(ErrorCode resultCode, String message, Throwable e) {
        super(message, e);
        this.resultCode = resultCode;
    }

    /**
     * 获取异常代码
     */
    public ErrorCode getErrorCode() {
        return resultCode;
    }

    /**
     * @see Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        if (null == super.getMessage()) {
            return this.resultCode.getMessage();
        } else {
            return super.getMessage();
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder(" GatewayliteException[");
        if (resultCode != null) {
            retValue.append("code=").append(resultCode.getCode()).append(',');
            retValue.append("description=").append(resultCode.getMessage()).append(',');
        }
        retValue.append("extraMessage=").append(getMessage());
        retValue.append(']');
        return retValue.toString();
    }
}
