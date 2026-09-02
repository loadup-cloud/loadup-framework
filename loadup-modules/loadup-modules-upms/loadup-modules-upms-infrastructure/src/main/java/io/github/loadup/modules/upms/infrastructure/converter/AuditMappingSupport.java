/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

package io.github.loadup.modules.upms.infrastructure.converter;

/** Converts domain audit values to the database representation. */
public final class AuditMappingSupport {
    private AuditMappingSupport() {}

    public static Integer toDeletedFlag(Boolean deleted) {
        return Boolean.TRUE.equals(deleted) ? 1 : 0;
    }

    public static Boolean toDeleted(Boolean deleted) {
        return deleted;
    }

    public static Boolean toDeleted(Integer deleted) {
        return deleted != null && deleted != 0;
    }
}
