/*
 * #%L
 * LoadUp File Client
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
package io.github.loadup.modules.file.client.facade;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.modules.file.client.dto.FileReferenceDTO;
import io.github.loadup.modules.file.client.dto.FileResourceDTO;
import java.io.InputStream;
import java.util.List;

/** Public business contract for FileResourceService. */
public interface FileResourceFacade {
    FileResourceDTO upload(
            String tenantId, String ownerId, String filename, String contentType, long size, InputStream content);

    FileResourceDTO get(String tenantId, String id, String actorId, boolean admin);

    FileDownloadResponse download(String tenantId, String id, String actorId, boolean admin);

    PageDTO<FileResourceDTO> list(String tenantId, String actorId, boolean admin, String ownerId, int page, int size);

    FileReferenceDTO attach(
            String tenantId, String id, String actorId, boolean admin, String referenceType, String referenceId);

    void detach(String tenantId, String id, String actorId, boolean admin, String referenceType, String referenceId);

    List<FileReferenceDTO> references(String tenantId, String id, String actorId, boolean admin);

    void requestDeletion(String tenantId, String id, String actorId, boolean admin);

    int cleanupPending(int limit);

    void cleanup(String tenantId, String id);
}
