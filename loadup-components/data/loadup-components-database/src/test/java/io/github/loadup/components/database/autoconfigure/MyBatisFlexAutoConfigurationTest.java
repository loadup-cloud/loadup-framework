/*
 * #%L
 * Loadup Components Database
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

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.id.UuidV7IdGenerator;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class MyBatisFlexAutoConfigurationTest {
    @Test
    void inheritsLogicalDeleteMetadataWithoutGlobalColumnName() {
        FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();
        String previous = config.getLogicDeleteColumn();
        try {
            config.setLogicDeleteColumn(null);
            var table = com.mybatisflex.core.table.TableInfoFactory.ofEntityClass(
                    io.github.loadup.components.database.sequence.dataobject.SequenceAllocationDO.class);
            assertThat(table.getLogicDeleteColumn()).isEqualTo("deleted");
        } finally {
            config.setLogicDeleteColumn(previous);
        }
    }

    @Test
    void usesFixedPersistenceConventions() {
        DatabaseProperties properties = new DatabaseProperties();
        MyBatisFlexAutoConfiguration autoConfiguration = new MyBatisFlexAutoConfiguration(properties);
        FlexGlobalConfig globalConfig = new FlexGlobalConfig();

        autoConfiguration
                .myBatisFlexCustomizer(provider(new UuidV7IdGenerator(false)), Clock.systemUTC())
                .customize(globalConfig);

        assertThat(globalConfig.getKeyConfig().getKeyType()).isEqualTo(KeyType.Generator);
        assertThat(globalConfig.getKeyConfig().getValue()).isEqualTo(IdGenerator.KEY);
        assertThat(KeyGeneratorFactory.getKeyGenerator(IdGenerator.KEY).generate(new Object(), "id"))
                .asString()
                .matches("[0-9a-f]{32}");
        assertThat(globalConfig.getLogicDeleteColumn()).isEqualTo("deleted");
        assertThat(globalConfig.getNormalValueOfLogicDelete()).isEqualTo(0);
        assertThat(globalConfig.getDeletedValueOfLogicDelete()).isEqualTo(1);
        assertThat(com.mybatisflex.core.logicdelete.LogicDeleteManager.getProcessor())
                .isInstanceOf(com.mybatisflex.core.logicdelete.impl.IntegerLogicDeleteProcessor.class);
    }

    private org.springframework.beans.factory.ObjectProvider<IdGenerator> provider(IdGenerator generator) {
        org.springframework.beans.factory.support.DefaultListableBeanFactory factory =
                new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        factory.registerSingleton("idGenerator", generator);
        return factory.getBeanProvider(IdGenerator.class);
    }

    @Test
    void resolvesStrategyOnlyWhenGeneratingKeys() {
        var initialized = new java.util.concurrent.atomic.AtomicInteger();
        var factory = new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        var definition = new org.springframework.beans.factory.support.RootBeanDefinition(IdGenerator.class, () -> {
            initialized.incrementAndGet();
            return (IdGenerator) () -> "deferred-id";
        });
        factory.registerBeanDefinition("idGenerator", definition);
        var configuration = new MyBatisFlexAutoConfiguration(new DatabaseProperties());
        configuration
                .myBatisFlexCustomizer(factory.getBeanProvider(IdGenerator.class), Clock.systemDefaultZone())
                .customize(new FlexGlobalConfig());
        assertThat(initialized.get()).isZero();
        assertThat(KeyGeneratorFactory.getKeyGenerator(IdGenerator.KEY).generate(new Object(), "id"))
                .isEqualTo("deferred-id");
        assertThat(initialized.get()).isEqualTo(1);
    }
}
