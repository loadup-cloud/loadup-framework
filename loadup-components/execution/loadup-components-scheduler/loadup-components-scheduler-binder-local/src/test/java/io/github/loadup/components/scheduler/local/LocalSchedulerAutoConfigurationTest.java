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
import static org.mockito.Mockito.mock;

import io.github.loadup.components.scheduler.SchedulerProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalSchedulerAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LocalSchedulerAutoConfiguration.class));

    @Test
    void localBinderIsSelectedByDefaultAndWhenExplicitlyConfigured() {
        runner.run(context -> assertThat(context).hasSingleBean(LocalSchedulerProvider.class));
        runner.withPropertyValues("loadup.scheduler.binder-type=local")
                .run(context -> assertThat(context).hasSingleBean(LocalSchedulerProvider.class));
    }

    @Test
    void otherBinderSkipsLocalProviderAndAUserDefinedLocalProviderTakesPrecedence() {
        runner.withPropertyValues("loadup.scheduler.binder-type=powerjob")
                .run(context -> assertThat(context).doesNotHaveBean(SchedulerProvider.class));

        var custom = mock(LocalSchedulerProvider.class);
        runner.withBean(LocalSchedulerProvider.class, () -> custom).run(context -> {
            assertThat(context).hasSingleBean(SchedulerProvider.class);
            assertThat(context.getBean(SchedulerProvider.class)).isSameAs(custom);
        });
    }
}
