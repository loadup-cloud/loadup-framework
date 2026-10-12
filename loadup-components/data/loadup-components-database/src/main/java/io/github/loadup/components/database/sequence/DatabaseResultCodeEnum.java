/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import io.github.loadup.commons.result.ResultCode;

public enum DatabaseResultCodeEnum implements ResultCode {
    INVALID_INPUT("Invalid sequence or business ID input"),
    EXHAUSTED("Sequence capacity exhausted"),
    UNAVAILABLE("Sequence allocation unavailable"),
    TIMEOUT("Sequence allocation timed out"),
    SCOPE_LIMIT("Active sequence scope limit reached"),
    CONFIG_CONFLICT("Sequence scope configuration conflicts"),
    CORRUPT_STATE("Sequence allocation state is invalid"),
    CLOSED("Sequence is closed");
    private final String message;

    DatabaseResultCodeEnum(String message) {
        this.message = message;
    }

    public String getCode() {
        return "DATABASE_SEQUENCE_" + name();
    }

    public String getStatus() {
        return "F";
    }

    public String getMessage() {
        return message;
    }
}
