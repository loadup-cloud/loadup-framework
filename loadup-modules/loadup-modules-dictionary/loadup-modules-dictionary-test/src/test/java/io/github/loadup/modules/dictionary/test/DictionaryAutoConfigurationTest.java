/*
 * #%L
 * LoadUp Dictionary Test
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
package io.github.loadup.modules.dictionary.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.modules.dictionary.app.autoconfigure.DictionaryAutoConfiguration;
import io.github.loadup.modules.dictionary.app.converter.DictionaryDTOConverter;
import io.github.loadup.modules.dictionary.app.service.DictionaryService;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.infrastructure.autoconfigure.DictionaryPersistenceAutoConfiguration;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryItemDOMapper;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryTypeDOMapper;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Exercises auto-configuration composition; no persistence operations are executed. */
class DictionaryAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean("dictionaryTypeDOMapper", DictionaryTypeDOMapper.class, () -> mock(DictionaryTypeDOMapper.class))
            .withBean("dictionaryItemDOMapper", DictionaryItemDOMapper.class, () -> mock(DictionaryItemDOMapper.class))
            .withConfiguration(AutoConfigurations.of(
                    DictionaryPersistenceAutoConfiguration.class, DictionaryAutoConfiguration.class));

    @Test
    void wiresDomainPortAndApplicationMapper() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(DictionaryGateway.class)
                    .hasSingleBean(DictionaryDTOConverter.class)
                    .hasSingleBean(DictionaryService.class);
        });
    }

    @Test
    void consumerGatewayOverridesDefaultAdapter() {
        DictionaryGateway gateway = mock(DictionaryGateway.class);
        runner.withBean(DictionaryGateway.class, () -> gateway).run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(DictionaryGateway.class)
                    .hasSingleBean(DictionaryService.class);
            assertThat(context.getBean(DictionaryGateway.class)).isSameAs(gateway);
        });
    }

    @Test
    void respectsDisabledFeature() {
        runner.withPropertyValues("loadup.modules.dictionary.enabled=false").run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(DictionaryGateway.class)
                    .doesNotHaveBean(DictionaryService.class);
        });
    }
}
