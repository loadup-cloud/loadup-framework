/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.http;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.ResultCode;

public class HttpCallException extends CommonException {
    private final Kind kind;

    public HttpCallException(Kind kind) {
        super(kind);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public enum Kind implements ResultCode {
        CONNECTION,
        TIMEOUT,
        TRANSPORT,
        SIZE_LIMIT,
        SERIALIZATION,
        RESPONSE_DECODING;

        public String getCode() {
            return "HTTP_" + name();
        }

        public String getStatus() {
            return ResultStatusEnum.FAIL.getCode();
        }

        public String getMessage() {
            return "HTTP call failed: " + name();
        }
    }
}
