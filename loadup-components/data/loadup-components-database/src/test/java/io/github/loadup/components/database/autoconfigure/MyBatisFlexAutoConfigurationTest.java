/*
 * #%L
 * LoadUp Components Database
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
package io.github.loadup.components.database.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import javax.sql.DataSource;
import org.apache.ibatis.session.LocalCacheScope;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MyBatisFlexAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MyBatisFlexAutoConfiguration.class))
            .withBean(DataSource.class, () -> mock(DataSource.class));

    @Test
    void appliesLogicalDeleteConventionsThroughTheCustomizer() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(MyBatisFlexCustomizer.class)
                    .hasSingleBean(ConfigurationCustomizer.class);
            var config = new FlexGlobalConfig();
            context.getBean(MyBatisFlexCustomizer.class).customize(config);
            assertThat(config.getLogicDeleteColumn()).isEqualTo("deleted");
            assertThat(config.getNormalValueOfLogicDelete()).isEqualTo(false);
            assertThat(config.getDeletedValueOfLogicDelete()).isEqualTo(true);
            assertThat(config.getDefaultPageSize()).isEqualTo(20);
            assertThat(config.getDefaultMaxPageSize()).isEqualTo(100);
        });
    }

    @Test
    void disablesSessionLocalCacheBecauseJdbcWritesBypassMyBatis() {
        runner.run(context -> {
            var configuration = new FlexConfiguration();
            context.getBean(ConfigurationCustomizer.class).customize(configuration);
            assertThat(configuration.getLocalCacheScope()).isEqualTo(LocalCacheScope.STATEMENT);
        });
    }

    @Test
    void backsOffWithoutSingleDataSource() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MyBatisFlexAutoConfiguration.class))
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(MyBatisFlexCustomizer.class)
                        .doesNotHaveBean(ConfigurationCustomizer.class));
    }
}
