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

import com.mybatisflex.core.keygen.IKeyGenerator;
import io.github.loadup.components.database.config.DatabaseProperties;
import java.util.Objects;

/** Adapts the configured LoadUp ID strategy to MyBatis-Flex. */
public final class DatabaseIdGenerator implements IdGenerator, IKeyGenerator {
    public static final String KEY = "loadupId";

    private final IdGenerator delegate;

    public DatabaseIdGenerator(DatabaseProperties.IdGenerator properties) {
        this.delegate = createDelegate(Objects.requireNonNull(properties));
    }

    @Override
    public String generate() {
        return delegate.generate();
    }

    @Override
    public Object generate(Object entity, String keyColumn) {
        return generate();
    }

    private static IdGenerator createDelegate(DatabaseProperties.IdGenerator properties) {
        return switch (properties.getStrategy()) {
            case RANDOM -> new RandomIdGenerator(properties.getRandomLength());
            case UUID_V4 -> new UuidV4IdGenerator(properties.isUuidWithHyphens());
            case UUID_V7 -> new UuidV7IdGenerator(properties.isUuidWithHyphens());
            case SNOWFLAKE ->
                new SnowflakeIdGenerator(properties.getSnowflakeWorkerId(), properties.getSnowflakeDatacenterId());
        };
    }
}
