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
package io.github.loadup.components.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TaskSubmissionTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t\n"})
    void identityFieldsCannotBeAbsentOrBlank(String invalid) {
        assertThatThrownBy(() -> TaskSubmission.immediate(invalid, 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", 1, invalid, "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identityLimitsAreInclusiveAndVersionMustBePositive() {
        TaskSubmission maximum = TaskSubmission.immediate("e".repeat(128), 1, "b".repeat(128), "");
        assertThat(maximum.payload()).isEmpty();
        assertThatThrownBy(() -> TaskSubmission.immediate("e".repeat(129), 1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", 1, "b".repeat(129), "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", 0, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", -1, "business", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void payloadLimitCountsUtf8BytesRatherThanCharacters() {
        String ascii = "a".repeat(1024 * 1024);
        assertThat(TaskSubmission.immediate("type", 1, "business", ascii).payload())
                .isEqualTo(ascii);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", 1, "business", ascii + "a"))
                .isInstanceOf(IllegalArgumentException.class);
        String unicode = "€".repeat(349525);
        assertThat(TaskSubmission.immediate("type", 1, "business", unicode).payload())
                .isEqualTo(unicode);
        assertThatThrownBy(() -> TaskSubmission.immediate("type", 1, "business", unicode + "€"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(TaskSubmission.immediate("type", 1, "business", null).payload())
                .isNull();
    }
}
