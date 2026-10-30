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
package io.github.loadup.components.scheduler.powerjob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.scheduler.ScheduledTask;
import io.github.loadup.components.scheduler.SchedulerAutoConfiguration;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tech.powerjob.worker.core.processor.TaskContext;

class PowerJobSchedulerTest {
    @Test
    void nativeProcessorDispatchesRegisteredScanner() throws Exception {
        var provider = new PowerJobSchedulerProvider();
        var context = new TaskContext();
        context.setJobParams("scan");
        assertThatThrownBy(() -> provider.process(context))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        var task = mock(ScheduledTask.class);
        when(task.name()).thenReturn("scan");
        provider.start(new SchedulerTemplate(provider, List.of(task)));
        assertThat(provider.process(context).isSuccess()).isTrue();
        verify(task).execute();
        provider.stop();
        assertThatThrownBy(() -> provider.process(context))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
    }

    @Test
    void configSelectsSingleBackend() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        PowerJobSchedulerAutoConfiguration.class, SchedulerAutoConfiguration.class))
                .withPropertyValues("loadup.scheduler.binder-type=powerjob")
                .run(context -> assertThat(context)
                        .hasSingleBean(SchedulerProvider.class)
                        .hasSingleBean(SchedulerTemplate.class));
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PowerJobSchedulerAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(SchedulerProvider.class));
    }
}
