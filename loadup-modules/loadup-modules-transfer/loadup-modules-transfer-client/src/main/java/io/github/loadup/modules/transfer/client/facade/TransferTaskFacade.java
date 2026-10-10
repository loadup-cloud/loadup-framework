/*
 * #%L
 * LoadUp Transfer Client
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
package io.github.loadup.modules.transfer.client.facade;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.transfer.client.dto.TransferTaskDTO;
import io.github.loadup.modules.transfer.client.enums.TransferKind;
import java.util.Map;

/** Public business contract for TransferTaskService. */
public interface TransferTaskFacade {
    TransferTaskDTO submit(
            String tenantId,
            String ownerId,
            TransferKind kind,
            String handlerKey,
            String sourceFileId,
            Map<String, String> options);

    TransferTaskDTO get(String tenantId, String id, String actorId, boolean admin);

    PageDTO<TransferTaskDTO> list(String tenantId, String actorId, boolean admin, String ownerId, int page, int size);

    TransferTaskDTO retry(String tenantId, String id, String actorId, boolean admin);
}
