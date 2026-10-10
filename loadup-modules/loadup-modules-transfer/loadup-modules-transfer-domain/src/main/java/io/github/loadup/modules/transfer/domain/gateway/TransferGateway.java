/*
 * #%L
 * LoadUp Transfer Domain
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
package io.github.loadup.modules.transfer.domain.gateway;

import io.github.loadup.modules.transfer.domain.model.TransferPage;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import java.util.Map;
import java.util.Optional;

public interface TransferGateway {
    void insert(TransferTask task, Map<String, String> options);

    Optional<TransferTask> find(String id);

    TransferPage<TransferTask> list(String tenantId, String ownerId, int page, int size);

    Map<String, String> options(String id);

    boolean start(String id);

    void progress(String id, long processed, long total);

    void succeed(String id, String resultFileId);

    void fail(String id, String message);

    boolean requeue(String id);
}
