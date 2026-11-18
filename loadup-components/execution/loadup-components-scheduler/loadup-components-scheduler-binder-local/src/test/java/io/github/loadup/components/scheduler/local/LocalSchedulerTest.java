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
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.scheduler.ScheduledTask;
import io.github.loadup.components.scheduler.SchedulerAutoConfiguration;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalSchedulerTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(LocalSchedulerAutoConfiguration.class, SchedulerAutoConfiguration.class));

    @Test
    void firesConfiguredCronAndKeepsOtherTasksIdle() throws Exception {
        var failing = mock(ScheduledTask.class);
        var fast = mock(ScheduledTask.class);
        var manual = mock(ScheduledTask.class);
        when(failing.name()).thenReturn("failing");
        when(fast.name()).thenReturn("fast");
        when(manual.name()).thenReturn("manual");
        // The local scheduler must keep firing after a handler fails.
        doThrow(new IllegalStateException("synthetic")).when(failing).execute();
        runner.withBean("failing", ScheduledTask.class, () -> failing)
                .withBean("fast", ScheduledTask.class, () -> fast)
                .withBean("manual", ScheduledTask.class, () -> manual)
                .withPropertyValues(
                        "loadup.scheduler.tasks[0].name=failing",
                        "loadup.scheduler.tasks[0].cron=* * * * * *",
                        "loadup.scheduler.tasks[1].name=fast",
                        "loadup.scheduler.tasks[1].cron=* * * * * *")
                .run(context -> {
                    assertThat(context).hasSingleBean(SchedulerTemplate.class);
                    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                        verify(failing, atLeastOnce()).execute();
                        verify(fast, atLeastOnce()).execute();
                    });
                    var scheduler = context.getBean(SchedulerTemplate.class);
                    scheduler.stop();
                    assertThat(scheduler.isRunning()).isFalse();
                    verify(manual, never()).execute();
                });
    }
}
