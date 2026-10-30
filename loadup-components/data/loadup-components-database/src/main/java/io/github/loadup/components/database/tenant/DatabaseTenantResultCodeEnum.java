/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
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
