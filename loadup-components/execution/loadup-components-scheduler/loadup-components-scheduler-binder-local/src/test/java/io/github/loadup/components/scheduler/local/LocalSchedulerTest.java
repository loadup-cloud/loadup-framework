/*
 * #%L
 * LoadUp Components Scheduler
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
package io.github.loadup.components.scheduler.local;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import io.github.loadup.components.scheduler.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalSchedulerTest {
    @Test
    void springScheduledWakesScannersWithoutExternalServices() {
        var calls = new AtomicInteger();
        new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(LocalSchedulerAutoConfiguration.class, SchedulerAutoConfiguration.class))
                .withBean(ScheduledTask.class, () -> new ScheduledTask() {
                    public String name() {
                        return "scan";
                    }

                    public void execute() {
                        calls.incrementAndGet();
                    }
                })
                .withPropertyValues(
                        "loadup.scheduler.local.interval=PT0.02S", "loadup.scheduler.local.initial-delay=PT0S")
                .run(context -> {
                    assertThat(context).hasSingleBean(SchedulerTemplate.class);
                    await().atMost(Duration.ofSeconds(3)).until(() -> calls.get() > 1);
                });
    }

    @Test
    void failuresAreIsolatedAndStoppedProviderIsIdle() {
        var provider = new LocalSchedulerProvider();
        provider.wakeUp();
        var calls = new AtomicInteger();
        var tasks = List.of(
                new ScheduledTask() {
                    public String name() {
                        return "bad";
                    }

                    public void execute() {
                        throw new IllegalStateException("synthetic");
                    }
                },
                new ScheduledTask() {
                    public String name() {
                        return "good";
                    }

                    public void execute() {
                        calls.incrementAndGet();
                    }
                });
        provider.start(new SchedulerTemplate(provider, tasks));
        provider.wakeUp();
        assertThat(calls).hasValue(1);
        provider.stop();
        provider.wakeUp();
        assertThat(calls).hasValue(1);
    }

    @Test
    void localBinderBacksOffForOtherSelection() {
        new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(LocalSchedulerAutoConfiguration.class, SchedulerAutoConfiguration.class))
                .withPropertyValues("loadup.scheduler.binder-type=powerjob")
                .run(context -> assertThat(context).doesNotHaveBean(LocalSchedulerProvider.class));
    }
}
