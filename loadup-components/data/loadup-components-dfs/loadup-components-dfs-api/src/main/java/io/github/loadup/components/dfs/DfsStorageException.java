/*
 * #%L
 * Loadup Dfs Components Api
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
package io.github.loadup.components.dfs;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.ResultCode;

/** Coded storage failure with a safe message and optional diagnostic cause. */
public class DfsStorageException extends CommonException {
    private static final long serialVersionUID = 1L;

    public enum Code implements ResultCode {
        OPERATION_FAILED,
        OBJECT_NOT_FOUND;

        public String getCode() {
            return "DFS_" + name();
        }

        public String getStatus() {
            return ResultStatusEnum.FAIL.getCode();
        }

        public String getMessage() {
            return this == OBJECT_NOT_FOUND ? "Storage object not found" : "Storage operation failed";
        }
    }

    public DfsStorageException(Code code) {
        super(code);
    }

    public DfsStorageException(Code code, Throwable cause) {
        super(code, cause);
    }
}
