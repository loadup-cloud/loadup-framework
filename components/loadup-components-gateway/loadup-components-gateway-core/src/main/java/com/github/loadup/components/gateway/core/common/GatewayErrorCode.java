package com.github.loadup.components.gateway.core.common;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.result.ResultCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Error Code
 */
public enum GatewayErrorCode implements ResultCode {

    /**
     * Success
     */
    SUCCESS("SUCCESS", "Success.", "S"),

    /**
     * System Exception
     */
    SYSTEM_EXCEPTION("SYSTEM_EXCEPTION", "Runtime system, like rpc time out, DB lock  time out etc", "U"),

    /**
     * Param illegal
     */
    PARAM_ILLEGAL("PARAM_ILLEGAL", "Param of input is illegal, like exceed max length, type not right etc.", "F"),

    /**
     * Param illegal
     */
    CONFIGURATION_NOT_FOUND("CONFIGURATION_NOT_FOUND", "Configuration can not be found, pls check.", "F"),

    /**
     * configuration load error
     */
    CONFIGURATION_LOAD_ERROR("CONFIGURATION_LOAD_ERROR", "Configuration load error, pls check.", "F"),

    /**
     * Unknown exception
     */
    UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "Unknown exception like NPE, ClassCastException etc.", "U"),

    /**
     * operation not support
     */
    OPERATION_NOT_SUPPORT("OPERATION_NOT_SUPPORT", "Operation not support.", "F"),

    /**
     * Invalid signature
     */
    INVALID_SIGNATURE("INVALID_SIGNATURE", "Signature is invalid.", "F"),

    /**
     * process fail
     */
    PROCESS_FAIL("PROCESS_FAIL", "General business failure. No retry.", "F"),

    /**
     * request traffic exceed limit
     */
    REQUEST_TRAFFIC_EXCEED_LIMIT("REQUEST_TRAFFIC_EXCEED_LIMIT", "Repeat traffic exceed inconsistent.", "F"),

    /**
     * json format illegal
     */
    JSON_FORMAT_ILLEGAL("JSON_FORMAT_ILLEGAL", "Json format is illegal.", "F"),

    /**
     * SERVICE_NOT_EXIST
     */
    SERVICE_NOT_EXIST("SERVICE_NOT_EXIST", "Service not exist, please confirm with the service provider.", "F"),

    /**
     * SCRIPT_LOAD_ERROR
     */
    SCRIPT_LOAD_ERROR("SCRIPT_LOAD_ERROR", "script load fail.", "F"),

    /**
     * SCRIPT_REMOVE_ERROR
     */
    SCRIPT_REMOVE_ERROR("SCRIPT_REMOVE_ERROR", "script remove fail", "F"),

    /**
     * WRITE_SCRIPT_CONFIG_ERROR
     */
    WRITE_SCRIPT_CONFIG_ERROR("WRITE_SCRIPT_CONFIG_ERROR", "write script config fail", "F"),

    /**
     * ILLEGAL_MESSAGE
     */
    ILLEGAL_MESSAGE("ILLEGAL_MESSAGE", "illegal message", "F"),

    /**
     * ASSEMBLER_ERROR
     */
    ASSEMBLER_ERROR("ASSEMBLER_ERROR", "assemble fail.", "F"),

    /**
     * SYSTEM_ERROR
     */
    SYSTEM_ERROR("SYSTEM_ERROR", "system error", "F"),

    /**
     * NOT_EXIST_ASSEMBLER
     */
    NOT_EXIST_ASSEMBLER("NOT_EXIST_ASSEMBLER", "not exist assembler", "F"),

    /**
     * INTERFACE_NOT_EXIST
     */
    INTERFACE_NOT_EXIST(" INTERFACE_NOT_EXIST", "interface not exist", "F"),

    /**
     * INTERFACE_STATUS_VALID
     */
    INTERFACE_STATUS_VALID("INTERFACE_STATUS_VALID", "interface status valid", "F"),

    /**
     * INTERFACE_NOT_NEWEST
     */
    INTERFACE_NOT_NEWEST("INTERFACE_NOT_NEWEST", "interface not newest", "F"),

    /**
     * INTERFACE_VERSION_NOT_BIGGEST
     */
    INTERFACE_VERSION_NOT_BIGGEST("INTERFACE_VERSION_NOT_BIGGEST", "Interface version is not the biggest.", "F"),

    /**
     * INTERFACE_ALREADY_EXISTS
     */
    INTERFACE_ALREADY_EXISTS("INTERFACE_ALREADY_EXISTS", "Interface already exists.", "S"),

    /**
     * CLIENT_ALREADY_EXIST
     */
    CLIENT_ALREADY_EXIST("CLIENT_ALREADY_EXIST", "client already exist", "S"),

    /**
     * CLIENT_ALREADY_EXIST
     */
    CLIENT_NOT_EXIST("CLIENT_NOT_EXIST", "client not exist", "F"),

    /**
     * SECURITY_ALREADY_EXIST
     */
    SECURITY_ALREADY_EXIST("SECURITY_ALREADY_EXIST", "security already exist", "S"),

    /**
     * SECURITY_NOT_EXIST
     */
    SECURITY_NOT_EXIST("SECURITY_NOT_EXIST", "security not exist", "F"),

    /**
     * ACCESS_DENIED
     */
    ACCESS_DENIED("ACCESS_DENIED", "Access denied.", "F"),

    /**
     * INVALID_API
     */
    INVALID_API("INVALID_API", "Invalid api.", "F"),

    /**
     * METHOD_NOT_SUPPORTED
     */
    METHOD_NOT_SUPPORTED("METHOD_NOT_SUPPORTED", "Method not supported.", "F"),

    /**
     * MEDIA_TYPE_NOT_ACCEPTABLE
     */
    MEDIA_TYPE_NOT_ACCEPTABLE("MEDIA_TYPE_NOT_ACCEPTABLE", "Media type not acceptable.", "F"),

    /**
     * INVALID_CLIENT
     */
    INVALID_CLIENT("INVALID_CLIENT", "Invalid client.", "F"),

    /**
     * illegal integration uri
     */
    ILLEGAL_INTEGRATION_URI("ILLEGAL_INTEGRATION_URI", "illegal integration uri.", "F");

    /**
     * error message
     */
    private final String resultMessage;

    /**
     * error code
     */
    private final String resultCode;

    private final String resultStatus;

    GatewayErrorCode(String resultCode, String resultMessage, String resultStatus) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.resultStatus = resultStatus;
    }

    /**
     * 获取枚举值
     */
    public static GatewayErrorCode getEnumByCode(String code) {
        for (GatewayErrorCode codeEnum : GatewayErrorCode.values()) {
            if (StringUtils.equals(code, codeEnum.getCode())) {
                return codeEnum;
            }
        }
        return null;
    }

    /**
     * Gets get code.
     */
    @Override
    public String getCode() {
        return this.resultCode;
    }

    /**
     * Gets get message.
     */
    @Override
    public String getMessage() {
        return this.resultMessage;
    }

    /**
     * Get the status
     */
    @Override
    public String getStatus() {
        return this.resultStatus;
    }
}
