/*
 * #%L
 * Loadup Components TestContainers
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
package io.github.loadup.components.testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import io.github.loadup.components.testcontainers.cloud.SharedLocalStackContainer;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.github.loadup.components.testcontainers.initializer.TestContainersPropertyInitializer;
import io.github.loadup.components.testcontainers.listener.TestContainersExecutionListener;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestContext;

class ContainerConfigurationTest {
    @Test
    void annotationStartupCollectsEveryTypeAndClearsThreadState() throws Exception {
        var listener = new TestContainersExecutionListener();
        var testContext = mock(TestContext.class);
        doReturn(AllContainers.class).when(testContext).getTestClass();
        try (var c0 = mockStatic(SharedMySQLContainer.class);
                var c1 = mockStatic(SharedPostgreSQLContainer.class);
                var c2 = mockStatic(SharedMongoDBContainer.class);
                var c3 = mockStatic(SharedRedisContainer.class);
                var c4 = mockStatic(SharedLocalStackContainer.class);
                var c5 = mockStatic(SharedKafkaContainer.class);
                var c6 = mockStatic(SharedElasticsearchContainer.class)) {
            c0.when(SharedMySQLContainer::getProperties).thenReturn(Map.of("fixture.0", "value-0"));
            c1.when(SharedPostgreSQLContainer::getProperties).thenReturn(Map.of("fixture.1", "value-1"));
            c2.when(SharedMongoDBContainer::getProperties).thenReturn(Map.of("fixture.2", "value-2"));
            c3.when(SharedRedisContainer::getProperties).thenReturn(Map.of("fixture.3", "value-3"));
            c4.when(SharedLocalStackContainer::getProperties).thenReturn(Map.of("fixture.4", "value-4"));
            c5.when(SharedKafkaContainer::getProperties).thenReturn(Map.of("fixture.5", "value-5"));
            c6.when(SharedElasticsearchContainer::getProperties).thenReturn(Map.of("fixture.6", "value-6"));
            listener.beforeTestClass(testContext);
            listener.beforeTestClass(testContext);
            c0.verify(() -> SharedMySQLContainer.startContainer(any(ContainerConfig.class)), times(1));
            c1.verify(() -> SharedPostgreSQLContainer.startContainer(any(ContainerConfig.class)), times(1));
            c2.verify(() -> SharedMongoDBContainer.startContainer(any(ContainerConfig.class)), times(1));
            c3.verify(() -> SharedRedisContainer.startContainer(any(ContainerConfig.class)), times(1));
            c4.verify(() -> SharedLocalStackContainer.startContainer(any(ContainerConfig.class)), times(1));
            c5.verify(() -> SharedKafkaContainer.startContainer(any(ContainerConfig.class)), times(1));
            c6.verify(() -> SharedElasticsearchContainer.startContainer(any(ContainerConfig.class)), times(1));
            assertThat(listener.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
            assertThat(TestContainersExecutionListener.getStoredProperties()).hasSize(7);
            try (var context = new GenericApplicationContext()) {
                new TestContainersPropertyInitializer().initialize(context);
                assertThat(context.getEnvironment().getProperty("fixture.0")).isEqualTo("value-0");
            }
        } finally {
            listener.afterTestClass(testContext);
        }
        assertThat(TestContainersExecutionListener.getStoredProperties()).isNull();
    }

    @Test
    void absentEmptyAndFailedAnnotationsDoNotLeaveProperties() throws Exception {
        var listener = new TestContainersExecutionListener();
        var context = mock(TestContext.class);
        try (var mysql = mockStatic(SharedMySQLContainer.class)) {
            doReturn(Unannotated.class).when(context).getTestClass();
            listener.beforeTestClass(context);
            doReturn(EmptyContainers.class).when(context).getTestClass();
            listener.beforeTestClass(context);
            mysql.verifyNoInteractions();
            doReturn(MySqlOnly.class).when(context).getTestClass();
            mysql.when(SharedMySQLContainer::getProperties).thenReturn(Map.of());
            listener.beforeTestClass(context);
            assertThat(TestContainersExecutionListener.getStoredProperties()).isNull();
            listener.afterTestClass(context);
            var failure = new IllegalStateException("synthetic startup failure");
            mysql.when(() -> SharedMySQLContainer.startContainer(any())).thenThrow(failure);
            assertThatThrownBy(() -> listener.beforeTestClass(context))
                    .isInstanceOf(CommonException.class)
                    .hasCause(failure);
            assertThat(TestContainersExecutionListener.getStoredProperties()).isNull();
        } finally {
            listener.afterTestClass(context);
        }
    }

    @Test
    void configurationModeStartsAllEnabledTypesAndInjectsProperties() {
        try (var c0 = mockStatic(SharedMySQLContainer.class);
                var c1 = mockStatic(SharedPostgreSQLContainer.class);
                var c2 = mockStatic(SharedMongoDBContainer.class);
                var c3 = mockStatic(SharedRedisContainer.class);
                var c4 = mockStatic(SharedLocalStackContainer.class);
                var c5 = mockStatic(SharedKafkaContainer.class);
                var c6 = mockStatic(SharedElasticsearchContainer.class);
                var context = new GenericApplicationContext()) {
            c0.when(SharedMySQLContainer::getProperties).thenReturn(Map.of("fixture.0", "value-0"));
            c1.when(SharedPostgreSQLContainer::getProperties).thenReturn(Map.of("fixture.1", "value-1"));
            c2.when(SharedMongoDBContainer::getProperties).thenReturn(Map.of("fixture.2", "value-2"));
            c3.when(SharedRedisContainer::getProperties).thenReturn(Map.of("fixture.3", "value-3"));
            c4.when(SharedLocalStackContainer::getProperties).thenReturn(Map.of("fixture.4", "value-4"));
            c5.when(SharedKafkaContainer::getProperties).thenReturn(Map.of("fixture.5", "value-5"));
            c6.when(SharedElasticsearchContainer::getProperties).thenReturn(Map.of("fixture.6", "value-6"));
            TestPropertyValues.of(
                            "loadup.testcontainers.enabled=true",
                            "loadup.testcontainers.mysql.enabled=true",
                            "loadup.testcontainers.postgresql.enabled=true",
                            "loadup.testcontainers.mongodb.enabled=true",
                            "loadup.testcontainers.redis.enabled=true",
                            "loadup.testcontainers.localstack.enabled=true",
                            "loadup.testcontainers.kafka.enabled=true",
                            "loadup.testcontainers.elasticsearch.enabled=true")
                    .applyTo(context);
            new TestContainersPropertyInitializer().initialize(context);
            c0.verify(() -> SharedMySQLContainer.startContainer(any(ContainerConfig.class)), times(1));
            c1.verify(() -> SharedPostgreSQLContainer.startContainer(any(ContainerConfig.class)), times(1));
            c2.verify(() -> SharedMongoDBContainer.startContainer(any(ContainerConfig.class)), times(1));
            c3.verify(() -> SharedRedisContainer.startContainer(any(ContainerConfig.class)), times(1));
            c4.verify(() -> SharedLocalStackContainer.startContainer(any(ContainerConfig.class)), times(1));
            c5.verify(() -> SharedKafkaContainer.startContainer(any(ContainerConfig.class)), times(1));
            c6.verify(() -> SharedElasticsearchContainer.startContainer(any(ContainerConfig.class)), times(1));
            for (int i = 0; i < 7; i++) {
                assertThat(context.getEnvironment().getProperty("fixture." + i)).isEqualTo("value-" + i);
            }
        }
    }

    @Test
    void absentDisabledAndUnselectedConfigurationsDoNotStartContainers() {
        try (var listener = mockStatic(TestContainersExecutionListener.class);
                var mysql = mockStatic(SharedMySQLContainer.class)) {
            for (String[] input :
                    new String[][] {{}, {"loadup.testcontainers.enabled=false"}, {"loadup.testcontainers.enabled=true"}
                    }) {
                try (var context = new GenericApplicationContext()) {
                    TestPropertyValues.of(input).applyTo(context);
                    new TestContainersPropertyInitializer().initialize(context);
                }
            }
            listener.when(TestContainersExecutionListener::getStoredProperties).thenReturn(Map.of());
            try (var context = new GenericApplicationContext()) {
                new TestContainersPropertyInitializer().initialize(context);
            }
            mysql.verifyNoInteractions();
        }
    }

    @Test
    void nullOptionalContainerConfigurationsAreIgnored() {
        var properties = new TestContainersProperties();
        properties.setMysql(null);
        properties.setPostgresql(null);
        properties.setMongodb(null);
        properties.setRedis(null);
        properties.setKafka(null);
        properties.setElasticsearch(null);
        properties.setLocalstack(null);
        try (var binderStatic = mockStatic(Binder.class);
                var context = new GenericApplicationContext()) {
            var binder = mock(Binder.class);
            binderStatic.when(() -> Binder.get(context.getEnvironment())).thenReturn(binder);
            BindResult<TestContainersProperties> result = mock(BindResult.class);
            when(result.orElse(null)).thenReturn(properties);
            when(binder.bind("loadup.testcontainers", TestContainersProperties.class))
                    .thenReturn(result);
            new TestContainersPropertyInitializer().initialize(context);
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isNull();
        }
    }

    @Test
    void baseInitializerHonorsBothSwitchesAndBindsConnectionSettings() {
        for (String[] switches : new String[][] {
            {},
            {"loadup.testcontainers.enabled=false", "loadup.testcontainers.mysql.enabled=true"},
            {"loadup.testcontainers.enabled=true"}
        }) {
            try (var context = new GenericApplicationContext()) {
                TestPropertyValues.of(switches).applyTo(context);
                var initializer = new FixtureInitializer();
                initializer.initialize(context);
                assertThat(initializer.started).isFalse();
            }
        }
        try (var context = new GenericApplicationContext()) {
            TestPropertyValues.of(
                            "loadup.testcontainers.mysql.enabled=true",
                            "loadup.testcontainers.mysql.image=mysql:8.0",
                            "loadup.testcontainers.mysql.database=merchant",
                            "loadup.testcontainers.mysql.username=reader",
                            "loadup.testcontainers.mysql.password=synthetic",
                            "loadup.testcontainers.mysql.reusable=false",
                            "loadup.testcontainers.reusable=true")
                    .applyTo(context);
            var initializer = new FixtureInitializer();
            initializer.initialize(context);
            assertThat(initializer.started).isTrue();
            assertThat(context.getEnvironment().getProperty("fixture.database")).isEqualTo("merchant");
            assertThat(initializer.config.getImage()).isEqualTo("mysql:8.0");
            assertThat(initializer.config.getUsername()).isEqualTo("reader");
            assertThat(initializer.config.getPassword()).isEqualTo("synthetic");
            assertThat(initializer.config.isReusable()).isFalse();
            assertThat(Binder.get(context.getEnvironment())
                            .bind("loadup.testcontainers", TestContainersProperties.class)
                            .get()
                            .isReusable())
                    .isTrue();
        }
    }

    static class FixtureInitializer extends BaseContainerInitializer {
        boolean started;
        ContainerConfig config;

        @Override
        protected String getContainerName() {
            return "fixture";
        }

        @Override
        protected ContainerConfig getContainerConfig(TestContainersProperties properties) {
            return properties.getMysql();
        }

        @Override
        protected void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment environment) {
            this.config = config;
            started = true;
            applyProperties(environment, Map.of("fixture.database", config.getDatabase()));
        }
    }

    @EnableTestContainers({
        ContainerType.MYSQL,
        ContainerType.POSTGRESQL,
        ContainerType.MONGODB,
        ContainerType.REDIS,
        ContainerType.LOCALSTACK,
        ContainerType.KAFKA,
        ContainerType.ELASTICSEARCH
    })
    static class AllContainers {}

    @EnableTestContainers({})
    static class EmptyContainers {}

    @EnableTestContainers(ContainerType.MYSQL)
    static class MySqlOnly {}

    static class Unannotated {}
}
