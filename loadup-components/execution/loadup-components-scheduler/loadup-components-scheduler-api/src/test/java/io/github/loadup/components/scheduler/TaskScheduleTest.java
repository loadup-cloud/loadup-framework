/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TaskScheduleTest {
    @ParameterizedTest
    @CsvSource({"0 0 9 15 * ?, 0 0 9 15 * ?", "0 0 9 ? * *, 0 0 9 * * ?", "0 0 9 ? * mon, 0 0 9 ? * MON"})
    void normalizesPortableDayFields(String configured, String normalized) {
        assertThat(new TaskSchedule("report.daily", configured).cron()).isEqualTo(normalized);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0 0 9 * *", "0 0 9 ? * 1", "0 0 9 15 * MON", "0 0 25 * * *"})
    void rejectsMissingOrNonPortableCron(String cron) {
        assertThatThrownBy(() -> new TaskSchedule("report.daily", cron))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid scheduler cron for task 'report.daily'");
    }

    @Test
    void rejectsAnInvalidTaskNameEvenWithAValidCron() {
        assertThatThrownBy(() -> new TaskSchedule("1report", "0 0 9 * * *"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid scheduler task name");
    }
}
