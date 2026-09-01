/*-
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

import java.util.Map;
import java.util.Objects;

/** Metadata used to initiate an S3-compatible multipart upload. */
public record MultipartUploadRequest(String filename, String contentType, String path, Map<String, String> metadata) {

    public MultipartUploadRequest {
        Objects.requireNonNull(filename, "filename must not be null");
        if (filename.isBlank()) {
            throw new IllegalArgumentException("filename must not be blank");
        }
        contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
