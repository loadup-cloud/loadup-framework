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
 * Serialization exception
 */
public class SerializationException extends GatewayException {

    private static final String MODULE = "SERIALIZATION";

    public SerializationException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.SERIALIZATION, MODULE, errorCode.getMessage() + ":" + message);
    }

    public SerializationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.SERIALIZATION, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static SerializationException jsonParseError(String json, Throwable cause) {
        return new SerializationException(ErrorCode.JSON_PARSE_ERROR, json, cause);
    }

    public static SerializationException jsonSerializeError(Object object, Throwable cause) {
        return new SerializationException(
                ErrorCode.JSON_SERIALIZE_ERROR, object.getClass().getSimpleName(), cause);
    }
}
