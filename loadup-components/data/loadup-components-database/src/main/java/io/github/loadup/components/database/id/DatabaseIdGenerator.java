/*-
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
package io.github.loadup.components.database.id;

import com.mybatisflex.core.keygen.IKeyGenerator;
import java.util.UUID;

/** Generates the framework UUID identifiers without a deployment-specific strategy. */
public final class DatabaseIdGenerator implements IdGenerator, IKeyGenerator {
    public static final String KEY = "loadupId";

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Object generate(Object entity, String keyColumn) {
        return generate();
    }
}
