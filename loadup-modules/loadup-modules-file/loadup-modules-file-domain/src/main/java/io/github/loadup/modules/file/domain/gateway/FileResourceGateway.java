/*
 * #%L
 * LoadUp File Domain
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
package io.github.loadup.modules.file.domain.gateway;

import io.github.loadup.modules.file.domain.model.FilePage;
import io.github.loadup.modules.file.domain.model.FileReference;
import io.github.loadup.modules.file.domain.model.FileResource;
import java.util.List;
import java.util.Optional;

public interface FileResourceGateway {
    void insert(FileResource file);

    Optional<FileResource> find(String tenantId, String id);

    Optional<FileResource> lock(String tenantId, String id);

    FilePage<FileResource> list(String tenantId, String ownerId, int page, int size);

    void markPending(String tenantId, String id);

    void markDeleted(String tenantId, String id);

    List<FileResource> pending(int limit);

    void addReference(FileReference reference);

    void removeReference(String tenantId, String fileId, String referenceType, String referenceId);

    long countReferences(String tenantId, String fileId);

    List<FileReference> references(String tenantId, String fileId);
}
