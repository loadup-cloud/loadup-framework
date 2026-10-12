/*
 * #%L
 * LoadUp Dictionary Client
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
package io.github.loadup.modules.dictionary.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/** Stable public error codes for the dictionary module. */
public enum DictionaryResultCodeEnum implements ResultCode {
    INVALID_PAGE("DICTIONARY_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    TYPE_IS_REQUIRED("DICTIONARY_TYPE_IS_REQUIRED", "type is required"),
    DICTIONARY_TYPE_CODE_ALREADY_EXISTS(
            "DICTIONARY_DICTIONARY_TYPE_CODE_ALREADY_EXISTS", "dictionary type code already exists"),
    TYPE_AND_ENABLED_ARE_REQUIRED("DICTIONARY_TYPE_AND_ENABLED_ARE_REQUIRED", "type and enabled are required"),
    DICTIONARY_TYPE_NOT_EMPTY(
            "DICTIONARY_DICTIONARY_TYPE_NOT_EMPTY", "delete dictionary items before deleting their type"),
    ITEM_IS_REQUIRED("DICTIONARY_ITEM_IS_REQUIRED", "item is required"),
    ITEM_ENABLED_AND_SORTORDER_ARE_REQUIRED(
            "DICTIONARY_ITEM_ENABLED_AND_SORTORDER_ARE_REQUIRED", "item, enabled and sortOrder are required"),
    DICTIONARY_TYPE_NOT_FOUND("DICTIONARY_DICTIONARY_TYPE_NOT_FOUND", "dictionary type not found"),
    DICTIONARY_ITEM_NOT_FOUND("DICTIONARY_DICTIONARY_ITEM_NOT_FOUND", "dictionary item not found"),
    INVALID_DICTIONARY_CODE(
            "DICTIONARY_INVALID_DICTIONARY_CODE",
            "code must start with a letter and contain only letters, digits, _, . or -"),
    DESCRIPTION_MUST_BE_AT_MOST("DICTIONARY_DESCRIPTION_MUST_BE_AT_MOST", "description must be at most ");
    private final String code;
    private final String message;

    DictionaryResultCodeEnum(String code, String message) {
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
        return ResultStatusEnum.FAIL.getCode();
    }
}
