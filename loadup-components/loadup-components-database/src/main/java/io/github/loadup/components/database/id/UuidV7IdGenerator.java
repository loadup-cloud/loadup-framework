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

import java.security.SecureRandom;
import java.util.UUID;

/** Generates RFC 9562 version 7 UUID identifiers. */
public final class UuidV7IdGenerator implements IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final boolean withHyphens;

    public UuidV7IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
    }

    @Override
    public String generate() {
        long timestamp = System.currentTimeMillis();
        long mostSignificantBits = (timestamp << 16) | 0x7000L | RANDOM.nextInt(1 << 12);
        long leastSignificantBits = RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL;
        leastSignificantBits |= 0x8000_0000_0000_0000L;
        String value = new UUID(mostSignificantBits, leastSignificantBits).toString();
        return withHyphens ? value : value.replace("-", "");
    }
}
