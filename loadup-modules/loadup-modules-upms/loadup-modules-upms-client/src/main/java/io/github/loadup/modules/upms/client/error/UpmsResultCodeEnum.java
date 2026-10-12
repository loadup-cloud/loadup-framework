/*
 * #%L
 * Loadup Modules UPMS Client Layer
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
package io.github.loadup.modules.upms.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/** Stable public error codes for the upms module. */
public enum UpmsResultCodeEnum implements ResultCode {
    UNSUPPORTED_USER_SORT_FIELD("UPMS_UNSUPPORTED_USER_SORT_FIELD", "Unsupported user sort field"),
    USERNAME_ALREADY_EXISTS("UPMS_USERNAME_ALREADY_EXISTS", "Username already exists"),
    EMAIL_IS_ALREADY_REGISTERED("UPMS_EMAIL_IS_ALREADY_REGISTERED", "Email is already registered"),
    PHONE_NUMBER_IS_ALREADY_REGISTERED("UPMS_PHONE_NUMBER_IS_ALREADY_REGISTERED", "Phone number is already registered"),
    USER_NOT_FOUND("UPMS_USER_NOT_FOUND", "User not found"),
    MASKED_VALUES_CANNOT_BE_SAVED("UPMS_MASKED_VALUES_CANNOT_BE_SAVED", "Masked values cannot be saved"),
    DEPARTMENT_DOES_NOT_EXIST_OR_IS_DISABLED(
            "UPMS_DEPARTMENT_DOES_NOT_EXIST_OR_IS_DISABLED", "Department does not exist or is disabled: "),
    ROLE_DOES_NOT_EXIST_OR_IS_DISABLED(
            "UPMS_ROLE_DOES_NOT_EXIST_OR_IS_DISABLED", "Role does not exist or is disabled: "),
    UNSUPPORTED_USER_SORT_FIELD_OR_DIRECTION(
            "UPMS_UNSUPPORTED_USER_SORT_FIELD_OR_DIRECTION", "Unsupported user sort field or direction"),
    INVALID_PASSWORD_CHANGE_REQUEST("UPMS_INVALID_PASSWORD_CHANGE_REQUEST", "invalid password change request"),
    ACCOUNT_IS_NOT_ACTIVE("UPMS_ACCOUNT_IS_NOT_ACTIVE", "account is not active"),
    CURRENT_PASSWORD_IS_INCORRECT("UPMS_CURRENT_PASSWORD_IS_INCORRECT", "Current password is incorrect"),
    PASSWORD_CONFIRMATION_DOES_NOT_MATCH(
            "UPMS_PASSWORD_CONFIRMATION_DOES_NOT_MATCH", "Password confirmation does not match"),
    PASSWORD_REUSE_FORBIDDEN("UPMS_PASSWORD_REUSE_FORBIDDEN", "new password must differ from old password"),
    ROLE_CODE_ALREADY_EXISTS("UPMS_ROLE_CODE_ALREADY_EXISTS", "Role code already exists"),
    ROLE_NOT_FOUND("UPMS_ROLE_NOT_FOUND", "Role not found"),
    ROLE_STILL_HAS_CHILD_ROLES("UPMS_ROLE_STILL_HAS_CHILD_ROLES", "Role still has child roles"),
    ROLE_IS_STILL_ASSIGNED_TO_USERS("UPMS_ROLE_IS_STILL_ASSIGNED_TO_USERS", "Role is still assigned to users"),
    UNSUPPORTED_SORT_DIRECTION("UPMS_UNSUPPORTED_SORT_DIRECTION", "Unsupported sort direction"),
    INVALID_PAGE("UPMS_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    UNSUPPORTED_ROLE_SORT_FIELD("UPMS_UNSUPPORTED_ROLE_SORT_FIELD", "Unsupported role sort field"),
    ROLE_IS_DISABLED("UPMS_ROLE_IS_DISABLED", "Role is disabled"),
    USER_DOES_NOT_EXIST("UPMS_USER_DOES_NOT_EXIST", "User does not exist"),
    PERMISSION_NOT_FOUND("UPMS_PERMISSION_NOT_FOUND", "Permission not found"),
    PERMISSION_DOES_NOT_EXIST("UPMS_PERMISSION_DOES_NOT_EXIST", "Permission does not exist: "),
    PERMISSION_IS_DISABLED("UPMS_PERMISSION_IS_DISABLED", "Permission is disabled: "),
    UNKNOWN_DATA_SCOPE("UPMS_UNKNOWN_DATA_SCOPE", "Unknown data scope: "),
    ROLE_HIERARCHY_CONTAINS_A_CYCLE("UPMS_ROLE_HIERARCHY_CONTAINS_A_CYCLE", "Role hierarchy contains a cycle"),
    PARENT_ROLE_DOES_NOT_EXIST("UPMS_PARENT_ROLE_DOES_NOT_EXIST", "Parent role does not exist: "),
    USER_NOT_FOUND_BBCDE8("UPMS_USER_NOT_FOUND_BBCDE8", "user not found"),
    INVALID_PAGE_OR_SIZE_0ED1C1("UPMS_INVALID_PAGE_OR_SIZE_0ED1C1", "invalid page or size"),
    USERID_IS_REQUIRED("UPMS_USERID_IS_REQUIRED", "userId is required"),
    INVALID_SENSITIVE_READ_REQUEST("UPMS_INVALID_SENSITIVE_READ_REQUEST", "Invalid sensitive read request"),
    PERMISSION_CODE_ALREADY_EXISTS("UPMS_PERMISSION_CODE_ALREADY_EXISTS", "Permission code already exists"),
    PERMISSION_CONTAINS_CHILD_PERMISSIONS(
            "UPMS_PERMISSION_CONTAINS_CHILD_PERMISSIONS", "Permission contains child permissions"),
    PERMISSION_HIERARCHY_CONTAINS_A_CYCLE(
            "UPMS_PERMISSION_HIERARCHY_CONTAINS_A_CYCLE", "Permission hierarchy contains a cycle"),
    PARENT_PERMISSION_DOES_NOT_EXIST("UPMS_PARENT_PERMISSION_DOES_NOT_EXIST", "Parent permission does not exist: "),
    DEPARTMENT_CODE_ALREADY_EXISTS("UPMS_DEPARTMENT_CODE_ALREADY_EXISTS", "Department code already exists"),
    PARENT_DEPARTMENT_NOT_FOUND("UPMS_PARENT_DEPARTMENT_NOT_FOUND", "Parent department not found"),
    DEPARTMENT_LEADER_NOT_FOUND("UPMS_DEPARTMENT_LEADER_NOT_FOUND", "Department leader not found"),
    DEPARTMENT_NOT_FOUND("UPMS_DEPARTMENT_NOT_FOUND", "Department not found"),
    DEPARTMENT_CANNOT_BE_ITS_OWN_PARENT(
            "UPMS_DEPARTMENT_CANNOT_BE_ITS_OWN_PARENT", "Department cannot be its own parent"),
    DEPARTMENT_CONTAINS_CHILD_DEPARTMENTS(
            "UPMS_DEPARTMENT_CONTAINS_CHILD_DEPARTMENTS", "Department contains child departments"),
    DEPARTMENT_CONTAINS_USERS("UPMS_DEPARTMENT_CONTAINS_USERS", "Department contains users"),
    NEW_PARENT_DEPARTMENT_NOT_FOUND("UPMS_NEW_PARENT_DEPARTMENT_NOT_FOUND", "New parent department not found"),
    DEPARTMENT_CYCLE("UPMS_DEPARTMENT_CYCLE", "Department cannot be moved below its descendants"),
    EMAIL_IS_NOT_REGISTERED("UPMS_EMAIL_IS_NOT_REGISTERED", "Email is not registered"),
    VERIFICATION_REQUESTS_ARE_RATE_LIMITED(
            "UPMS_VERIFICATION_REQUESTS_ARE_RATE_LIMITED", "Verification requests are rate limited"),
    PHONE_NUMBER_IS_NOT_REGISTERED("UPMS_PHONE_NUMBER_IS_NOT_REGISTERED", "Phone number is not registered"),
    EMAIL_IS_NOT_BOUND_TO_THE_USER("UPMS_EMAIL_IS_NOT_BOUND_TO_THE_USER", "Email is not bound to the user"),
    PHONE_NUMBER_IS_NOT_BOUND_TO_THE_USER(
            "UPMS_PHONE_NUMBER_IS_NOT_BOUND_TO_THE_USER", "Phone number is not bound to the user"),
    UNSUPPORTED_VERIFICATION_METHOD("UPMS_UNSUPPORTED_VERIFICATION_METHOD", "Unsupported verification method"),
    VERIFICATION_CODE_IS_INVALID_OR_EXPIRED(
            "UPMS_VERIFICATION_CODE_IS_INVALID_OR_EXPIRED", "Verification code is invalid or expired"),
    EMAIL_IS_REQUIRED("UPMS_EMAIL_IS_REQUIRED", "Email is required"),
    VERIFICATION_CODE_IS_REQUIRED("UPMS_VERIFICATION_CODE_IS_REQUIRED", "Verification code is required"),
    INVALID_LOGIN_CREDENTIALS("UPMS_INVALID_LOGIN_CREDENTIALS", "Invalid login credentials"),
    ACCOUNT_IS_LOCKED_OR_DISABLED("UPMS_ACCOUNT_IS_LOCKED_OR_DISABLED", "Account is locked or disabled"),
    UTILITY_CLASS("UPMS_UTILITY_CLASS", "Utility class"),
    OPERATION_FAILED("UPMS_OPERATION_FAILED", "Operation failed"),
    OPERATION_NOT_ALLOWED("UPMS_OPERATION_NOT_ALLOWED", "Operation not allowed"),
    ACCOUNT_IS_LOCKED("UPMS_ACCOUNT_IS_LOCKED", "Account is locked"),
    MOBILE_IS_REQUIRED("UPMS_MOBILE_IS_REQUIRED", "Mobile is required"),
    UNSUPPORTED_LOGIN_TYPE("UPMS_UNSUPPORTED_LOGIN_TYPE", "Unsupported login type"),
    PASSWORD_CHANGE_REQUEST_IS_REQUIRED(
            "UPMS_PASSWORD_CHANGE_REQUEST_IS_REQUIRED", "password change request is required"),
    AUTHENTICATED_USER_IS_REQUIRED("UPMS_AUTHENTICATED_USER_IS_REQUIRED", "authenticated user is required"),
    AUTHENTICATED_USER_IS_REQUIRED_74D75F(
            "UPMS_AUTHENTICATED_USER_IS_REQUIRED_74D75F", "Authenticated user is required"),
    INVALID_PASSWORD(
            "UPMS_INVALID_PASSWORD", "Password requires at least 8 Unicode characters and at most 72 UTF-8 bytes"),
    INVALID_USER_STATUS("UPMS_INVALID_USER_STATUS", "Invalid user status: ");
    private final String code;
    private final String message;

    UpmsResultCodeEnum(String code, String message) {
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
