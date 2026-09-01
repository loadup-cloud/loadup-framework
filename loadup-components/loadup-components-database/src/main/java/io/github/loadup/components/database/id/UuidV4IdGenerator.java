/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

package io.github.loadup.components.database.id;

import java.util.UUID;

/** Generates RFC 4122 version 4 UUID identifiers. */
public final class UuidV4IdGenerator implements IdGenerator {
    private final boolean withHyphens;

    public UuidV4IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
    }

    @Override
    public String generate() {
        String value = UUID.randomUUID().toString();
        return withHyphens ? value : value.replace("-", "");
    }
}
