/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.config;

import io.github.loadup.commons.json.ToStringAsJson;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Default metadata for automatic persistence identifiers.
 */
@ConfigurationProperties("loadup.database.business-id")
public record BusinessIdProperties(
        @DefaultValue("0") int dataVersion,
        @DefaultValue("1") int systemVersion,
        @DefaultValue("00000000") String eventCode,
        @DefaultValue("") String extensionCode,
        @DefaultValue("0") int siteId,
        @DefaultValue("0") int shard) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
