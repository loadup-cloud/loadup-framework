/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/**
 * Request-entry failures for the deployment code header.
 *
 * <p>These codes describe a deployment request error only. They carry no database tenant or schema
 * meaning, and the former tenant resolver errors must not be reused for them.
 */
public enum DeploymentResultCodeEnum implements ResultCode {
    DEPLOYMENT_CODE_REQUIRED("DEPLOYMENT_CODE_REQUIRED", "Deployment code header is required"),
    DEPLOYMENT_CODE_INVALID("DEPLOYMENT_CODE_INVALID", "Deployment code header is malformed or repeated"),
    DEPLOYMENT_CODE_MISMATCH("DEPLOYMENT_CODE_MISMATCH", "Deployment code does not match this deployment");

    private final String code;
    private final String message;

    DeploymentResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return ResultStatusEnum.FAIL.getCode();
    }

    @Override
    public String getMessage() {
        return message;
    }

    /** 400 for missing, blank, malformed, repeated or comma-combined values. */
    public int httpStatus() {
        return this == DEPLOYMENT_CODE_MISMATCH ? 403 : 400;
    }
}
