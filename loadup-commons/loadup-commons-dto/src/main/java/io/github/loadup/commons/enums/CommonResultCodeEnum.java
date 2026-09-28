package io.github.loadup.commons.enums;

import io.github.loadup.commons.result.ResultCode;
import java.util.Arrays;
import org.apache.commons.lang3.Strings;

public enum CommonResultCodeEnum implements ResultCode {
    SUCCESS(ResultStatusEnum.SUCCESS, "Success."),
    UNKNOWN(ResultStatusEnum.UNKNOWN, "Unknown failed."),
    PARAM_ILLEGAL(ResultStatusEnum.FAIL, "Parameter illegal."),
    PROCESS_FAIL(ResultStatusEnum.FAIL, "Process fail."),
    ACCESS_DENIED(ResultStatusEnum.FAIL, "Access denied."),
    UNAUTHENTICATED(ResultStatusEnum.FAIL, "Authentication required or invalid token."),
    INVALID_CLIENT(ResultStatusEnum.FAIL, "Invalid client."),
    NOT_FOUND(ResultStatusEnum.FAIL, "Key is not found."),
    SYS_ERROR(ResultStatusEnum.FAIL, "System error."),
    ;
    private final String status;

    private final String message;

    CommonResultCodeEnum(ResultStatusEnum status, String message) {
        this.status = status.getCode();
        this.message = message;
    }

    public static CommonResultCodeEnum getByResultCode(String resultCode) {
        return Arrays.stream(CommonResultCodeEnum.values())
                .filter(value -> Strings.CI.equals(value.getCode(), resultCode))
                .findFirst()
                .orElse(CommonResultCodeEnum.SYS_ERROR);
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
