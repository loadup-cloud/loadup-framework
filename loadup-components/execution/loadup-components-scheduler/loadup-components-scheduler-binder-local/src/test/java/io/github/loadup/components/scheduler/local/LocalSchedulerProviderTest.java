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
package io.github.loadup.components.scheduler.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.scheduler.TaskSchedule;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LocalSchedulerProviderTest {
    @Test
    void emptySchedulesAndRepeatedStopsAreNoOps() {
        var provider = new LocalSchedulerProvider();

        provider.start(List.of(), name -> {});
        provider.stop();
        provider.stop();
    }

    @Test
    void impossibleScheduleFailsStartupAndAllowsLaterUse() throws InterruptedException {
        var provider = new LocalSchedulerProvider();
        var impossible = new TaskSchedule("bad", "0 0 0 31 FEB ?");

        try {
            assertThatThrownBy(() -> provider.start(List.of(impossible), name -> {}))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("No future cron occurrence for bad");

            var called = new CountDownLatch(1);
            provider.start(List.of(new TaskSchedule("scan", "* * * * * *")), name -> called.countDown());
            assertThat(called.await(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            provider.stop();
        }
    }
}
