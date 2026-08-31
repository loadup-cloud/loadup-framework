package io.github.loadup.components.globalunique.service;

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

/**
 * Idempotency facade backed by a database unique-key constraint.
 *
 * <p>Call inside the business transaction: a successful insert claims the key, a duplicate
 * insert is treated as an idempotent replay. When the surrounding transaction rolls back, the
 * claim is rolled back too and the request can be retried.
 */
public interface GlobalUniqueService {

    /**
     * Claims the unique key, returning {@code true} on first insert.
     *
     * @param uniqueKey business-defined unique key, e.g. {@code "ORDER_CREATE:userId:orderId"}
     * @param bizType business type for classification, e.g. {@code "ORDER"}, {@code "PAYMENT"}
     * @return {@code true} when the key was claimed (proceed with business logic),
     *     {@code false} when the key already exists (idempotent replay)
     */
    boolean insertAndCheck(String uniqueKey, String bizType);

    /**
     * Claims the unique key with an optional business id.
     *
     * @param uniqueKey business-defined unique key
     * @param bizType business type
     * @param bizId optional business id for later lookup
     * @return {@code true} when the key was claimed, {@code false} on duplicate
     */
    boolean insertAndCheck(String uniqueKey, String bizType, String bizId);

    /**
     * Claims the unique key with an optional business id and request snapshot.
     *
     * @param uniqueKey business-defined unique key
     * @param bizType business type
     * @param bizId optional business id
     * @param requestData optional JSON request snapshot for troubleshooting
     * @return {@code true} when the key was claimed, {@code false} on duplicate
     */
    boolean insertAndCheck(String uniqueKey, String bizType, String bizId, String requestData);
}
