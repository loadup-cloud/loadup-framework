/*
 * #%L
 * LoadUp Lock
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
package io.github.loadup.components.lock;

import io.github.loadup.commons.error.CommonException;

/** Acquisition ended without a lock; the business callback was not executed. */
public final class LockUnavailableException extends CommonException {
    public enum Code implements io.github.loadup.commons.result.ResultCode {
        ACQUISITION_TIMEOUT;

        public String getCode() {
            return "LOCK_ACQUISITION_TIMEOUT";
        }

        public String getStatus() {
            return io.github.loadup.commons.enums.ResultStatusEnum.FAIL.getCode();
        }

        public String getMessage() {
            return "Lock acquisition wait timeout";
        }
    }

    public LockUnavailableException() {
        super(Code.ACQUISITION_TIMEOUT);
    }
}
