/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.client.dto;

import io.github.loadup.commons.result.ResultCode;

public enum ContractError implements ResultCode {
    NOT_FOUND("CONTRACT_NOT_FOUND", "Contract resource not found"),
    CONFLICT("CONTRACT_CONFLICT", "Contract resource changed or already exists"),
    INVALID_STATE("CONTRACT_INVALID_STATE", "Operation not allowed in current state"),
    MERCHANT_UNVERIFIED("CONTRACT_MERCHANT_UNVERIFIED", "Verified active merchant is required");
    private final String code;
    private final String message;

    ContractError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return "F";
    }
}
