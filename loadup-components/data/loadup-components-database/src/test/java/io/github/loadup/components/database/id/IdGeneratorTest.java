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
package io.github.loadup.components.database.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class IdGeneratorTest {
    @Test
    void generatesUuidIdentifiers() {
        var generator = new UuidV4IdGenerator(true);
        var first = generator.generate();
        assertThat(java.util.UUID.fromString(first).version()).isEqualTo(4);
        assertThat(generator.generate()).isNotEqualTo(first);
    }

    @Test
    void validatesRandomLength() {
        assertThatIllegalArgumentException().isThrownBy(() -> new RandomIdGenerator(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new RandomIdGenerator(65));
    }

    @Test
    void validatesSnowflakeWorkerAndDatacenterIds() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(-1, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(0, 32));
        assertThat(new SnowflakeIdGenerator(1, 2).generate()).matches("\\d+");
    }

    @Test
    void uuidVariantsKeepVersionAndRfcVariantWithOrWithoutSeparators() {
        for (boolean hyphens : new boolean[] {true, false}) {
            for (IdGenerator generator :
                    new IdGenerator[] {new UuidV4IdGenerator(hyphens), new UuidV7IdGenerator(hyphens)}) {
                String value = generator.generate();
                assertThat(value).hasSize(hyphens ? 36 : 32);
                String canonical = hyphens
                        ? value
                        : value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16) + "-"
                                + value.substring(16, 20) + "-" + value.substring(20);
                var uuid = java.util.UUID.fromString(canonical);
                assertThat(uuid.variant()).isEqualTo(2);
                assertThat(uuid.version()).isEqualTo(generator instanceof UuidV4IdGenerator ? 4 : 7);
            }
        }
    }

    @Test
    void randomIdsRespectBothLengthBoundariesAndAlphabet() {
        assertThat(new RandomIdGenerator(1).generate()).matches("[0-9a-zA-Z]");
        assertThat(new RandomIdGenerator(64).generate()).matches("[0-9a-zA-Z]{64}");
        assertThatIllegalArgumentException().isThrownBy(() -> new RandomIdGenerator(-1));
    }

    @Test
    void snowflakeBoundaryNodesHaveDistinctIdsAndMonotonicSequences() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(32, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(0, -1));
        var firstNode = new SnowflakeIdGenerator(0, 0);
        var lastNode = new SnowflakeIdGenerator(31, 31);
        long previous = -1;
        java.util.Set<String> generated = new java.util.HashSet<>();
        for (int i = 0; i < 10000; i++) {
            String id = firstNode.generate();
            long value = Long.parseLong(id);
            assertThat(value).isGreaterThan(previous);
            assertThat(generated.add(id)).isTrue();
            assertThat(generated.add(lastNode.generate())).isTrue();
            previous = value;
        }
    }
}
