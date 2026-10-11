/*
 * #%L
 * Loadup Dfs Components Api
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
package io.github.loadup.components.dfs.model;

import java.time.Instant;
import java.util.Map;

/** Immutable metadata returned by every DFS binder. */
public record FileMetadata(
        String fileId,
        String filename,
        long size,
        String contentType,
        String provider,
        String path,
        Map<String, String> metadata,
        Instant uploadedAt) {

    public FileMetadata {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
