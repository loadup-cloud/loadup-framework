/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

package io.github.loadup.components.database.listener;

/** Raised when a required tenant context is missing. */
public class TenantContextMissingException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public TenantContextMissingException(Class<?> entityType) {
        super("Tenant context is required for entity " + entityType.getName());
    }

    public TenantContextMissingException(String tableName) {
        super("Tenant context is required for table " + tableName);
    }
}
