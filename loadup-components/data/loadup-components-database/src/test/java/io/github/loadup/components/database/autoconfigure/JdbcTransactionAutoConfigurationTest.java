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
package io.github.loadup.components.database.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.mybatisflex.spring.boot.v4.FlexTransactionAutoConfiguration;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

class JdbcTransactionAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JdbcTransactionAutoConfiguration.class));

    @Test
    void createsManagerForSingleDataSource() {
        var source = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(JdbcTransactionManager.class);
            assertThat(context.getBean(JdbcTransactionManager.class).getDataSource())
                    .isSameAs(source);
        });
    }

    @Test
    void selectsJdbcManagerBeforeMyBatisFlexFallback() {
        var source = mock(DataSource.class);
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        FlexTransactionAutoConfiguration.class, JdbcTransactionAutoConfiguration.class))
                .withBean(DataSource.class, () -> source)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(PlatformTransactionManager.class);
                    assertThat(context).hasSingleBean(JdbcTransactionManager.class);
                    assertThat(context).doesNotHaveBean(FlexTransactionAutoConfiguration.class);
                    assertThat(context.getBean(JdbcTransactionManager.class).getDataSource())
                            .isSameAs(source);
                });
    }

    @Test
    void respectsApplicationTransactionManager() {
        var manager = mock(PlatformTransactionManager.class);
        runner.withBean(DataSource.class, () -> mock(DataSource.class))
                .withBean(PlatformTransactionManager.class, () -> manager)
                .run(context -> {
                    assertThat(context).hasNotFailed().doesNotHaveBean(JdbcTransactionManager.class);
                    assertThat(context.getBean(PlatformTransactionManager.class))
                            .isSameAs(manager);
                });
    }

    @Test
    void doesNotChooseAmongMultipleDataSources() {
        runner.withBean("first", DataSource.class, () -> mock(DataSource.class))
                .withBean("second", DataSource.class, () -> mock(DataSource.class))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(JdbcTransactionManager.class));
    }
}
