package io.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import io.github.loadup.commons.dto.DTO;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.enums.ResultStatusEnum;
import java.io.Serial;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lise
 * @since 1.0.0
 */
public class Result implements DTO, ResultCode, Serializable {

    @Serial
    private static final long serialVersionUID = -6017673453793484984L;

    private String code;

    /**
     * resultCode
     */
    private String status;

    /**
     * resultMsg
     */
    private String message;

    public static Result buildSuccess() {
        Result result = new Result();
        result.setCode(CommonResultCodeEnum.SUCCESS.getCode());
        result.setStatus(ResultStatusEnum.SUCCESS.getCode());
        result.setMessage(CommonResultCodeEnum.SUCCESS.getMessage());
        return result;
    }

    public static Result buildFailure() {
        Result result = new Result();
        result.setCode(CommonResultCodeEnum.PROCESS_FAIL.getCode());
        result.setStatus(CommonResultCodeEnum.PROCESS_FAIL.getStatus());
        result.setMessage(CommonResultCodeEnum.PROCESS_FAIL.getMessage());
        return result;
    }

    public static Result buildFailure(String errorCode, String errorMessage) {
        Result result = new Result();
        CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
        result.setCode(resultCodeEnum.getCode());
        result.setStatus(resultCodeEnum.getStatus());
        result.setMessage(resultCodeEnum.getMessage());
        if (StringUtils.isNotBlank(errorMessage)) {
            result.setMessage(errorMessage);
        }
        return result;
    }

    public static Result buildFailure(String errorCode) {
        Result result = new Result();
        CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
        result.setCode(resultCodeEnum.getCode());
        result.setStatus(resultCodeEnum.getStatus());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static Result buildFailure(ResultCode errorCode, String errorMessage) {
        Result result = new Result();
        result.setCode(errorCode.getCode());
        result.setStatus(errorCode.getStatus());
        result.setMessage(errorCode.getMessage());
        if (StringUtils.isNotBlank(errorMessage)) {
            result.setMessage(errorMessage);
        }
        return result;
    }

    public static Result buildFailure(ResultCode errorCode) {
        Result result = new Result();
        result.setCode(errorCode.getCode());
        result.setStatus(errorCode.getStatus());
        result.setMessage(errorCode.getMessage());
        return result;
    }

    public String getCode() {
        return this.code;
    }

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
