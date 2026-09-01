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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.github.loadup.components.database.config.DatabaseProperties;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {
    @Test
    void generatesConfiguredStrategies() {
        for (DatabaseProperties.Strategy strategy : DatabaseProperties.Strategy.values()) {
            DatabaseProperties.IdGenerator properties = new DatabaseProperties.IdGenerator();
            properties.setStrategy(strategy);
            String id = new DatabaseIdGenerator(properties).generate();

            assertThat(id).isNotBlank().hasSizeBetween(1, 64);
            if (strategy == DatabaseProperties.Strategy.UUID_V4) {
                assertThat(id).hasSize(32).doesNotContain("-");
            }
            if (strategy == DatabaseProperties.Strategy.UUID_V7) {
                assertThat(id).hasSize(32);
                assertThat(id.charAt(12)).isEqualTo('7');
            }
        }
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
}
