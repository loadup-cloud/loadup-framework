/*-
 * #%L
 * LoadUp Components :: Global Unique
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

package io.github.loadup.components.globalunique;

import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import java.util.Optional;

/** Claims tenant-scoped idempotency keys through a database unique constraint. */
public interface GlobalUniqueTemplate {

    /**
     * Claims a business key in the current tenant.
     *
     * @param bizType business namespace
     * @param uniqueKey unique key inside the namespace
     * @return {@code true} for the first claim, otherwise {@code false}
     */
    boolean claim(String bizType, String uniqueKey);

    /**
     * Claims a business key and stores optional diagnostic data.
     *
     * @param claim claim data
     * @return {@code true} for the first claim, otherwise {@code false}
     */
    boolean claim(GlobalUniqueClaim claim);

    /**
     * Finds a claim in the current tenant.
     *
     * @param bizType business namespace
     * @param uniqueKey unique key inside the namespace
     * @return the matching record when present
     */
    Optional<GlobalUniqueRecord> find(String bizType, String uniqueKey);
}
