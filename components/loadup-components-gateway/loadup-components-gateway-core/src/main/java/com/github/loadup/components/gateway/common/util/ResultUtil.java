package com.github.loadup.components.gateway.common.util;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.facade.model.Result;

/**
 *
 */
public class ResultUtil {

    /**
     *
     */
    public static Result buildResult(GatewayliteErrorCode errorCode, String message) {

        Result result = new Result();
        result.setResultCode(errorCode.getCode());
        result.setResultStatus(errorCode.getStatus());
        result.setResultMessage(errorCode.getMessage());
        return result;

    }

    /**
     *
     */
    public static Result buildResult(GatewayliteErrorCode errorCode) {
        return buildResult(errorCode, errorCode.getMessage());
    }

    /**
     *
     */
    public static Result buildResult(ErrorCode errorCode) {
        Result result = new Result();
        result.setResultCode(errorCode.getCode());
        result.setResultStatus(errorCode.getStatus());
        result.setResultMessage(errorCode.getMessage());
        return result;
    }

    /**
     *
     */
    public static Result buildSuccessResult() {
        return buildResult(GatewayliteErrorCode.SUCCESS);
    }

}