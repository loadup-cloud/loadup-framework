/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.webmvc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("loadup.webmvc.request-body")
public record RequestBodyLimitProperties(
        @DefaultValue("/api/internal/") String pathPrefix, DataSize maxSize) {
    public RequestBodyLimitProperties {
        if (pathPrefix == null
                || !pathPrefix.startsWith("/")
                || !pathPrefix.endsWith("/")
                || pathPrefix.contains("//")
                || pathPrefix.contains("..")
                || pathPrefix.contains("%")) {
            throw new IllegalArgumentException("Request body path prefix must be an absolute directory path");
        }
        if (maxSize == null || maxSize.toBytes() < 1 || maxSize.toBytes() >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Request body max size must be between 1 and 2147483646 bytes");
        }
    }
}
