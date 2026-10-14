/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OutboxMessageTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t\n"})
    void identityFieldsCannotBeAbsentOrBlank(String invalid) {
        assertThatThrownBy(() -> new OutboxMessage(invalid, "type", 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", invalid, 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 1, invalid, "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identityLimitsAreInclusiveAndVersionMustBePositive() {
        OutboxMessage maximum = new OutboxMessage("t".repeat(64), "e".repeat(128), 1, "b".repeat(128), "");
        assertThat(maximum.payload()).isEmpty();
        assertThatThrownBy(() -> new OutboxMessage("t".repeat(65), "type", 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "e".repeat(129), 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 1, "b".repeat(129), "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 0, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", -1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void payloadLimitCountsUtf8BytesRatherThanCharacters() {
        String ascii = "a".repeat(1024 * 1024);
        assertThat(new OutboxMessage("tenant", "type", 1, "business", ascii).payload())
                .isEqualTo(ascii);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 1, "business", ascii + "a"))
                .isInstanceOf(IllegalArgumentException.class);
        String unicode = "\u20ac".repeat(349525);
        assertThat(new OutboxMessage("tenant", "type", 1, "business", unicode).payload())
                .isEqualTo(unicode);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 1, "business", unicode + "\u20ac"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxMessage("tenant", "type", 1, "business", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
