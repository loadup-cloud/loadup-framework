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

/** Generates compact random identifiers. */
public final class RandomIdGenerator implements IdGenerator {
    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int length;

    public RandomIdGenerator(int length) {
        if (length < 1 || length > 64) {
            throw new IllegalArgumentException("Random ID length must be between 1 and 64");
        }
        this.length = length;
    }

    @Override
    public String generate() {
        char[] result = new char[length];
        for (int index = 0; index < result.length; index++) {
            result[index] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(result);
    }
}
