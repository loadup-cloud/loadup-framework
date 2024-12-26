package com.github.loadup.components.gateway.service.template;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.facade.model.Result;
import com.github.loadup.components.gateway.facade.request.BaseRequest;
import com.github.loadup.components.gateway.facade.response.BaseResponse;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.validation.ConstraintViolation;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * batch service template, include: </br>
 * <li>uniform process flow</li>
 * <li>exception process</li>
 * <li>log print</li>
 */
public final class ServiceTemplate {

    /**
     * biz logger
     */
    private final static Logger bizErrorLogger = LoggerFactory
            .getLogger("DIGEST-MESSAGE-LOGGER");

    /**
     * execute
     */
    public static void execute(BaseRequest request, BaseResponse result, ServiceCallback callback) {
        try {

            validateRequest(request);

            callback.checkParameter();

            callback.preProcess();

            callback.process();

            fillSuccessResult(result);
        } catch (GatewayException ex) {

            // print warn log
            LogUtil.warn(bizErrorLogger, ex,
                    "gateway service process, biz exception occured:", ex.getMessage());
            // print debug log if necessary
            LogUtil.debug(bizErrorLogger, ex);
            ErrorCode errorCode = ex.getErrorCode() == null ? GatewayliteErrorCode.UNKNOWN_EXCEPTION
                    : ex.getErrorCode();

            Result result1 = new Result();
            result1.setResultCode(errorCode.getCode());
            result1.setResultStatus(errorCode.getStatus());
            result1.setResultMessage(errorCode.getMessage());

            result.setResult(result1);

            result.setErrorMessage(ex.getMessage());
        } catch (Throwable throwable) {

            // print warn log
            LogUtil.error(bizErrorLogger, throwable,
                    "gateway service process, unknown exception occured:",
                    throwable.getMessage());
            // print debug log if necessary
            LogUtil.debug(bizErrorLogger, throwable);

            ErrorCode errorCode = GatewayliteErrorCode.UNKNOWN_EXCEPTION;

            Result result1 = new Result();
            result1.setResultCode(errorCode.getCode());
            result1.setResultStatus(errorCode.getStatus());
            result1.setResultMessage(errorCode.getMessage());

            result.setResult(result1);

            result.setErrorMessage(throwable.getMessage());
        }

    }

    /**
     * validateRequest
     */
    private static void validateRequest(BaseRequest request) {

        Set<ConstraintViolation<BaseRequest>> resultSet;
        try {
            // check biz request
            //            ValidationUtils.v();
            resultSet = null;// FastValidatorUtils.validate(request);
        } catch (Exception exception) {
            throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
                    GatewayliteErrorCode.PARAM_ILLEGAL.getMessage(), exception);
        }

        if (CollectionUtils.isNotEmpty(resultSet)) {
            ConstraintViolation<BaseRequest> violation = resultSet.iterator().next();
            throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
                    violation.getMessage(), null);
        }
    }

    /**
     * fill the results of successful processing
     */
    public static void fillSuccessResult(BaseResponse response) {
        response.setSuccess(true);
        response.setResult(buildSuccessResult());
        response.setErrorMessage(GatewayliteErrorCode.SUCCESS.getMessage());
    }

    private static Result buildSuccessResult() {
        Result result = new Result();
        result.setResultCode(GatewayliteErrorCode.SUCCESS.getCode());
        result.setResultMessage(GatewayliteErrorCode.SUCCESS.getMessage());
        result.setResultStatus(GatewayliteErrorCode.SUCCESS.getStatus());
        return result;
    }

}