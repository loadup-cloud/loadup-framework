/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.database.config;

import io.github.loadup.components.database.schema.FixedSchemaNames;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Fixed database target of one deployment.
 *
 * <p>{@code loadup.database.schema} is the only schema source for the connection pool, the runtime
 * transaction manager and the migration target. There is no tenant map, no default tenant and no
 * schema fallback to {@code public}.
 */
@ConfigurationProperties(prefix = "loadup.database")
@Validated
public class DatabaseProperties {

    /** Business schema that owns every table, sequence, outbox and task of this application. */
    @NotBlank @Pattern(regexp = FixedSchemaNames.PATTERN) private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema == null ? null : FixedSchemaNames.require(schema);
    }
}
