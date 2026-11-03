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

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.SuccessResponse;

public final class HttpServiceResponses {
    private HttpServiceResponses() {}

    public static <T> T requiredData(SuccessResponse<T> response) {
        if (response == null || response.result() == null) {
            throw new HttpCallException(HttpCallException.Kind.RESPONSE_DECODING);
        }
        var result = response.result();
        if (CommonResultCodeEnum.SUCCESS.getCode().equals(result.getCode())
                && CommonResultCodeEnum.SUCCESS.getStatus().equals(result.getStatus())
                && response.data() != null) {
            return response.data();
        }
        if (ResultStatusEnum.FAIL.getCode().equals(result.getStatus())
                && response.data() == null
                && result.getCode() != null
                && result.getCode().matches("[A-Z][A-Z0-9_]{0,63}")
                && !CommonResultCodeEnum.SUCCESS.getCode().equals(result.getCode())) {
            throw new RemoteFailure(result.getCode());
        }
        throw new HttpCallException(HttpCallException.Kind.RESPONSE_DECODING);
    }

    /** Callers translate only their own explicit whitelist; an arbitrary exception carries no remote code. */
    public static String resultCode(Throwable failure) {
        return failure instanceof RemoteFailure remote ? remote.code : "";
    }

    private static final class RemoteFailure extends HttpCallException {
        private final String code;

        private RemoteFailure(String code) {
            super(Kind.TRANSPORT);
            this.code = code;
        }
    }
}
