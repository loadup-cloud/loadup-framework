/*
 * #%L
 * LoadUp Components Database
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
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

public enum DatabaseTenantResultCodeEnum implements ResultCode {
    TENANT_REQUIRED("Tenant context is required for database access"),
    INVALID_SCHEMA("Tenant schema name is invalid");

    private final String message;

    DatabaseTenantResultCodeEnum(String message) {
        this.message = message;
    }

    public String getCode() {
        return "DATABASE_TENANT_" + name();
    }

    public String getStatus() {
        return ResultStatusEnum.FAIL.getCode();
    }

    public String getMessage() {
        return message;
    }
}
