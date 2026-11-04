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
package io.github.loadup.components.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.json.JsonUtil;
import io.github.loadup.components.runtime.context.ApplicationContextUtils;
import io.github.loadup.components.runtime.context.RuntimeContextAutoConfiguration;
import io.github.loadup.components.runtime.json.JsonAutoConfiguration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

class RuntimeAssemblyTest {
    @Test
    void configuresNonWebJsonWithoutReplacingTheCommonsMapper() {
        var commonsMapper = JsonUtil.getObjectMapper();
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JsonAutoConfiguration.class, JacksonAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(JsonMapper.class);
                    var mapper = context.getBean(JsonMapper.class);
                    assertThat(mapper).isNotSameAs(commonsMapper);
                    assertThat(mapper.writeValueAsString(LocalDate.of(2026, 11, 4)))
                            .isEqualTo("\"2026-11-04\"");
                    assertThat(JsonUtil.getObjectMapper()).isSameAs(commonsMapper);
                });
    }

    @Test
    void registersTheContextUtilityWithoutComponentScanning() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RuntimeContextAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ApplicationContextUtils.class);
                    assertThat(ApplicationContextUtils.getEnvironment()).isSameAs(context.getEnvironment());
                });
    }
}
