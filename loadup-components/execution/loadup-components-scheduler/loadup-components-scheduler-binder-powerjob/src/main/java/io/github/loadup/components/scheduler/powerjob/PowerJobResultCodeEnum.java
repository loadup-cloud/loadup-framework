/*
 * #%L
 * LoadUp Components Scheduler Binder PowerJob
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
package io.github.loadup.components.scheduler.powerjob;

import io.github.loadup.commons.result.ResultCode;

public enum PowerJobResultCodeEnum implements ResultCode {
    INVALID_CLIENT_CONFIGURATION("PowerJob client configuration is invalid"),
    PROCESSOR_FAILED("PowerJob processor reported failure"),
    INVALID_PROCESSOR_RESULT("PowerJob processor returned no result");
    private final String message;

    @Override
    public String getStatus() {
        return io.github.loadup.commons.enums.ResultStatusEnum.FAIL.getCode();
    }

    PowerJobResultCodeEnum(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return "POWERJOB_" + name();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
