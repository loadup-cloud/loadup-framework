/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult;

import io.github.loadup.commons.error.CommonException;

/**
 * Remote codes are data; applications explicitly map their own known error enums.
 */
public final class AsyncResultFailureException extends CommonException {
    private final String remoteCode;

    public AsyncResultFailureException(String remoteCode) {
        super(AsyncResultCodeEnum.REMOTE_FAILURE);
        this.remoteCode = remoteCode;
    }

    public String getRemoteCode() {
        return remoteCode;
    }
}
