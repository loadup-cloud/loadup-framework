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
 * Route related exception
 */
public class RouteException extends GatewayException {

    private static final String MODULE = "ROUTE";

    public RouteException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.ROUTING, MODULE, errorCode.getMessage() + ":" + message);
    }

    public RouteException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.ROUTING, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static RouteException notFound(String path) {
        return new RouteException(ErrorCode.ROUTE_NOT_FOUND, path);
    }

    public static RouteException invalidPath(String path) {
        return new RouteException(ErrorCode.ROUTE_INVALID_PATH, path);
    }

    public static RouteException configError(String message) {
        return new RouteException(ErrorCode.ROUTE_CONFIG_ERROR, message);
    }
}
