package io.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

/**
 * Proxy-related exceptions
 */
public class ProxyException extends GatewayException {

    private static final String MODULE = "PROXY";

    public ProxyException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.PROXY, MODULE, errorCode.getMessage() + ":" + message);
    }

    public ProxyException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.PROXY, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods - SpringBean proxy exceptions
    public static ProxyException beanNotFound(String beanName) {
        return new ProxyException(ErrorCode.BEAN_NOT_FOUND, beanName);
    }

    public static ProxyException methodNotFound(String beanName, String methodName) {
        return new ProxyException(ErrorCode.BEAN_METHOD_NOT_FOUND, beanName + "." + methodName);
    }

    public static ProxyException methodInvokeFailed(String beanName, String methodName, Throwable cause) {
        return new ProxyException(ErrorCode.BEAN_METHOD_INVOKE_FAILED, beanName + "." + methodName, cause);
    }

    public static ProxyException invalidTarget(String target) {
        return new ProxyException(
                ErrorCode.BEAN_TARGET_FORMAT_INVALID, target + ", Expected format is beanName:methodName");
    }

    // Convenience methods - HTTP proxy exceptions
    public static ProxyException httpRequestFailed(String url, Throwable cause) {
        return new ProxyException(ErrorCode.HTTP_REQUEST_FAILED, url, cause);
    }

    public static ProxyException httpTimeout(String url) {
        return new ProxyException(ErrorCode.HTTP_CONNECTION_TIMEOUT, url);
    }

    // Convenience methods - RPC proxy exceptions
    public static ProxyException rpcServiceNotFound(String serviceName) {
        return new ProxyException(ErrorCode.RPC_SERVICE_NOT_FOUND, serviceName);
    }

    public static ProxyException rpcCallFailed(String serviceName, String methodName, Throwable cause) {
        return new ProxyException(
                ErrorCode.RPC_CALL_FAILED, "RPC call failed: " + serviceName + "." + methodName, cause);
    }

    // General proxy exceptions
    public static ProxyException executionFailed(String target, Throwable cause) {
        return new ProxyException(ErrorCode.PROXY_EXECUTION_FAILED, "Proxy execution failed: " + target, cause);
    }

    public static ProxyException timeout(String target) {
        return new ProxyException(ErrorCode.PROXY_TIMEOUT, "Proxy timeout: " + target);
    }
}
