/*
 * #%L
 * LoadUp Contract App
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
package io.github.loadup.modules.contract.app.support;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.contract.client.error.ContractResultCodeEnum;

public final class ContractIdentity {
    private ContractIdentity() {}

    public static String tenant() {
        String tenant = TenantUtil.getTenantId();
        if (tenant == null || tenant.isBlank() || tenant.length() > 64)
            throw new CommonException(ContractResultCodeEnum.TRUSTED_TENANT_CONTEXT_REQUIRED);
        return tenant;
    }

    public static void actor(String actor) {
        if (actor == null || actor.isBlank() || actor.length() > 256)
            throw new CommonException(ContractResultCodeEnum.AUTHENTICATED_ACTOR_REQUIRED);
    }

    public static void page(int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new CommonException(ContractResultCodeEnum.INVALID_PAGE);
    }

    public static String code(String value, int max) {
        if (value == null || value.length() > max || !value.matches("[A-Za-z0-9][A-Za-z0-9_.-]*"))
            throw new CommonException(ContractResultCodeEnum.INVALID_IDENTIFIER_2E6368);
        return value;
    }
}
