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
package io.github.loadup.components.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.cache.AbstractRedisContainerTest;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import io.github.loadup.components.testcontainers.cloud.SharedLocalStackContainer;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import io.github.loadup.components.testcontainers.database.AbstractMongoDBContainerTest;
import io.github.loadup.components.testcontainers.database.AbstractMySQLContainerTest;
import io.github.loadup.components.testcontainers.database.AbstractPostgreSQLContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.github.loadup.components.testcontainers.messaging.AbstractKafkaContainerTest;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import io.github.loadup.components.testcontainers.search.AbstractElasticsearchContainerTest;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

class ContainerLifecycleTest {
    private static final String GLOBAL_SWITCH = "loadup.testcontainers.enabled";
    private static final String LOCALSTACK_SWITCH = "loadup.testcontainers.localstack.enabled";

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @ParameterizedTest(name = "{0} startup can retry and publishes connection properties")
    @EnumSource(ContainerType.class)
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void startupCanRetryAndPublishesConnectionProperties(ContainerType type) throws Throwable {
        try (var loader = newContainerClassLoader()) {
            if (type == ContainerType.LOCALSTACK) {
                initializeLocalStack(loader, true, true);
            }
            var checks = loader.loadClass(ContainerLifecycleChecks.class.getName());
            var verifyStartup = checks.getDeclaredMethod("verifyStartup", ContainerType.class);
            verifyStartup.setAccessible(true);
            try {
                verifyStartup.invoke(null, type);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void localStackDisabledModesAreEvaluatedInFreshClassLoaders() throws Exception {
        for (boolean globalEnabled : new boolean[] {false, true}) {
            try (var loader = newContainerClassLoader()) {
                Class<?> isolated = initializeLocalStack(loader, globalEnabled, !globalEnabled);
                assertThat(isolated.getMethod("isEnabled").invoke(null)).isEqualTo(false);
                assertThatThrownBy(() -> isolated.getMethod("getInstance").invoke(null))
                        .isInstanceOf(InvocationTargetException.class)
                        .hasCauseInstanceOf(IllegalStateException.class);
            }
        }
    }

    private static Class<?> initializeLocalStack(ClassLoader loader, boolean globalEnabled, boolean localEnabled)
            throws ClassNotFoundException {
        String global = System.getProperty(GLOBAL_SWITCH);
        String local = System.getProperty(LOCALSTACK_SWITCH);
        try {
            System.setProperty(GLOBAL_SWITCH, Boolean.toString(globalEnabled));
            System.setProperty(LOCALSTACK_SWITCH, Boolean.toString(localEnabled));
            return Class.forName(SharedLocalStackContainer.class.getName(), true, loader);
        } finally {
            restoreProperty(GLOBAL_SWITCH, global);
            restoreProperty(LOCALSTACK_SWITCH, local);
        }
    }

    private static URLClassLoader newContainerClassLoader() {
        String checksName = ContainerLifecycleChecks.class.getName();
        String componentPackage = ContainerLifecycleTest.class.getPackageName() + ".";
        var location = SharedLocalStackContainer.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        var checksLocation = ContainerLifecycleChecks.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        return new URLClassLoader(new URL[] {location, checksLocation}, ContainerLifecycleTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    String simpleName = name.substring(name.lastIndexOf('.') + 1);
                    boolean isolated = name.equals(checksName)
                            || name.startsWith(checksName + "$")
                            || name.startsWith(componentPackage)
                                    && (simpleName.startsWith("Shared") && simpleName.endsWith("Container")
                                            || simpleName.startsWith("Abstract")
                                                    && simpleName.endsWith("ContainerTest"));
                    if (!isolated) {
                        return super.loadClass(name, resolve);
                    }
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        loaded = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
            }
        };
    }
}

final class ContainerLifecycleChecks {
    private ContainerLifecycleChecks() {}

    static void verifyStartup(ContainerType type) {
        switch (type) {
            case MYSQL -> mysqlStartupCanRetryAndPublishesConnectionProperties();
            case POSTGRESQL -> postgresStartupCanRetryAndPublishesConnectionProperties();
            case MONGODB -> mongoStartupCanRetryAndPublishesConnectionProperties();
            case REDIS -> redisStartupCanRetryAndPublishesConnectionProperties();
            case KAFKA -> kafkaStartupCanRetryAndPublishesConnectionProperties();
            case ELASTICSEARCH -> elasticsearchStartupCanRetryAndPublishesConnectionProperties();
            case LOCALSTACK -> localstackStartupCanRetryAndPublishesConnectionProperties();
        }
    }

    private static ContainerConfig config(String image) {
        var config = new ContainerConfig();
        config.setImage(image);
        config.setEnabled(true);
        config.setDatabase("merchant");
        config.setUsername("reader");
        config.setPassword("synthetic");
        config.setReusable(false);
        return config;
    }

    private static void mysqlStartupCanRetryAndPublishesConnectionProperties() {
        assertThatThrownBy(SharedMySQLContainer::getJdbcUrl).isInstanceOf(IllegalStateException.class);
        assertThat(SharedMySQLContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                MySQLContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getJdbcUrl()).thenReturn("jdbc:mysql://localhost:13306/merchant");
                    when(container.getUsername()).thenReturn("reader");
                    when(container.getPassword()).thenReturn("synthetic");
                    when(container.getDatabaseName()).thenReturn("merchant");
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getMappedPort(MySQLContainer.MYSQL_PORT)).thenReturn(13306);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("mysql:8.0");
            assertThatThrownBy(() -> SharedMySQLContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedMySQLContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedMySQLContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThatThrownBy(() -> SharedMySQLContainer.startContainer(null)).hasMessage("synthetic startup failure");
            fail.set(false);
            SharedMySQLContainer.startContainer(config("mysql:8.0"));
            SharedMySQLContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(4);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedMySQLContainer.isStarted()).isTrue();
            assertThat(SharedMySQLContainer.getInstance()).isSameAs(container);
            assertThat(SharedMySQLContainer.getProperties())
                    .containsEntry("spring.datasource.url", "jdbc:mysql://localhost:13306/merchant");
            assertThat(SharedMySQLContainer.getHost()).isEqualTo("localhost");
            assertThat(SharedMySQLContainer.getMappedPort()).isEqualTo(13306);
            new MySqlFixture().check();
        }
    }

    private static void postgresStartupCanRetryAndPublishesConnectionProperties() {
        assertThatThrownBy(SharedPostgreSQLContainer::getJdbcUrl).isInstanceOf(IllegalStateException.class);
        assertThat(SharedPostgreSQLContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                PostgreSQLContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getJdbcUrl()).thenReturn("jdbc:postgresql://localhost:15432/merchant");
                    when(container.getUsername()).thenReturn("reader");
                    when(container.getPassword()).thenReturn("synthetic");
                    when(container.getDatabaseName()).thenReturn("merchant");
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getFirstMappedPort()).thenReturn(15432);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("postgres:16-alpine");
            assertThatThrownBy(() -> SharedPostgreSQLContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedPostgreSQLContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedPostgreSQLContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedPostgreSQLContainer.startContainer(config("postgres:16-alpine"));
            SharedPostgreSQLContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedPostgreSQLContainer.isStarted()).isTrue();
            assertThat(SharedPostgreSQLContainer.getInstance()).isSameAs(container);
            assertThat(SharedPostgreSQLContainer.getProperties())
                    .containsEntry("spring.datasource.url", "jdbc:postgresql://localhost:15432/merchant");
            new PostgresFixture().check();
        }
    }

    private static void mongoStartupCanRetryAndPublishesConnectionProperties() {
        assertThat(SharedMongoDBContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                MongoDBContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getConnectionString()).thenReturn("mongodb://localhost:27017");
                    when(container.getReplicaSetUrl()).thenReturn("mongodb://localhost:27017/?replicaSet=rs0");
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getFirstMappedPort()).thenReturn(27017);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("mongo:7.0");
            assertThatThrownBy(() -> SharedMongoDBContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedMongoDBContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedMongoDBContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedMongoDBContainer.startContainer(config("mongo:7.0"));
            SharedMongoDBContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedMongoDBContainer.isStarted()).isTrue();
            assertThat(SharedMongoDBContainer.getInstance()).isSameAs(container);
            assertThat(SharedMongoDBContainer.getProperties()).containsEntry("spring.data.mongodb.port", "27017");
            new MongoFixture().check();
        }
    }

    private static void redisStartupCanRetryAndPublishesConnectionProperties() {
        assertThat(SharedRedisContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                GenericContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getMappedPort(6379)).thenReturn(16379);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("redis:7-alpine");
            assertThatThrownBy(() -> SharedRedisContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedRedisContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedRedisContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedRedisContainer.startContainer(config("redis:7-alpine"));
            SharedRedisContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedRedisContainer.isStarted()).isTrue();
            assertThat(SharedRedisContainer.getInstance()).isSameAs(container);
            assertThat(SharedRedisContainer.getProperties()).containsEntry("spring.data.redis.port", "16379");
            new RedisFixture().check();
        }
    }

    private static void kafkaStartupCanRetryAndPublishesConnectionProperties() {
        assertThatThrownBy(SharedKafkaContainer::getBootstrapServers).isInstanceOf(IllegalStateException.class);
        assertThat(SharedKafkaContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                KafkaContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getBootstrapServers()).thenReturn("localhost:19092");
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getFirstMappedPort()).thenReturn(19092);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("apache/kafka:4.1.1");
            assertThatThrownBy(() -> SharedKafkaContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedKafkaContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedKafkaContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedKafkaContainer.startContainer(config("apache/kafka:4.1.1"));
            SharedKafkaContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedKafkaContainer.isStarted()).isTrue();
            assertThat(SharedKafkaContainer.getInstance()).isSameAs(container);
            assertThat(SharedKafkaContainer.getProperties())
                    .containsEntry("spring.kafka.bootstrap-servers", "localhost:19092");
            new KafkaFixture().check();
        }
    }

    private static void elasticsearchStartupCanRetryAndPublishesConnectionProperties() {
        assertThatThrownBy(SharedElasticsearchContainer::getHttpHostAddress).isInstanceOf(IllegalStateException.class);
        assertThat(SharedElasticsearchContainer.isStarted()).isFalse();
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                ElasticsearchContainer.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (container, context) -> {
                    when(container.getHttpHostAddress()).thenReturn("localhost:19200");
                    when(container.getHost()).thenReturn("localhost");
                    when(container.getFirstMappedPort()).thenReturn(19200);
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("docker.elastic.co/elasticsearch/elasticsearch:8.11.0");
            assertThatThrownBy(() -> SharedElasticsearchContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            assertThat(SharedElasticsearchContainer.isStarted()).isFalse();
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedElasticsearchContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedElasticsearchContainer.startContainer(config("docker.elastic.co/elasticsearch/elasticsearch:8.11.0"));
            SharedElasticsearchContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedElasticsearchContainer.isStarted()).isTrue();
            assertThat(SharedElasticsearchContainer.getInstance()).isSameAs(container);
            assertThat(SharedElasticsearchContainer.getProperties())
                    .containsEntry("spring.elasticsearch.uris", "localhost:19200");
            new ElasticsearchFixture().check();
        }
    }

    private static void localstackStartupCanRetryAndPublishesConnectionProperties() {
        AtomicBoolean fail = new AtomicBoolean(true);
        try (var containers = mockConstruction(
                LocalStackContainer.class, withSettings().defaultAnswer(Answers.RETURNS_SELF), (container, context) -> {
                    when(container.getEndpoint()).thenReturn(URI.create("http://localhost:14566"));
                    when(container.getAccessKey()).thenReturn("synthetic-access");
                    when(container.getSecretKey()).thenReturn("synthetic-secret");
                    when(container.getRegion()).thenReturn("us-east-1");
                    doAnswer(invocation -> {
                                if (fail.get()) {
                                    throw new IllegalStateException("synthetic startup failure");
                                }
                                return null;
                            })
                            .when(container)
                            .start();
                })) {
            ContainerConfig config = config("localstack/localstack:3.0");
            assertThatThrownBy(() -> SharedLocalStackContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            config.setImage(null);
            config.setDatabase("");
            config.setUsername("");
            config.setPassword("");
            assertThatThrownBy(() -> SharedLocalStackContainer.startContainer(config))
                    .hasMessage("synthetic startup failure");
            fail.set(false);
            SharedLocalStackContainer.startContainer(config("localstack/localstack:3.0"));
            SharedLocalStackContainer.startContainer(config);
            assertThat(containers.constructed()).hasSize(3);
            var container = containers.constructed().getLast();
            verify(container, times(1)).start();
            verify(container).withReuse(false);
            assertThat(SharedLocalStackContainer.getInstance()).isSameAs(container);
            assertThat(SharedLocalStackContainer.getProperties())
                    .isEqualTo(Map.of(
                            "aws.s3.endpoint", "http://localhost:14566",
                            "aws.access-key-id", "synthetic-access",
                            "aws.secret-access-key", "synthetic-secret",
                            "aws.region", "us-east-1",
                            "spring.cloud.aws.s3.endpoint", "http://localhost:14566",
                            "spring.cloud.aws.credentials.access-key", "synthetic-access",
                            "spring.cloud.aws.credentials.secret-key", "synthetic-secret",
                            "spring.cloud.aws.region.static", "us-east-1"));
            assertThat(SharedLocalStackContainer.isEnabled()).isEqualTo(true);
        }
    }

    static class MySqlFixture extends AbstractMySQLContainerTest {
        void check() {
            assertThat(getJdbcUrl()).isEqualTo(SharedMySQLContainer.getJdbcUrl());
            assertThat(getUsername()).isEqualTo("reader");
            assertThat(getPassword()).isEqualTo("synthetic");
            assertThat(getDatabaseName()).isEqualTo("merchant");
        }
    }

    static class PostgresFixture extends AbstractPostgreSQLContainerTest {
        void check() {
            assertThat(getJdbcUrl()).isEqualTo(SharedPostgreSQLContainer.getJdbcUrl());
            assertThat(getUsername()).isEqualTo("reader");
            assertThat(getPassword()).isEqualTo("synthetic");
            assertThat(getDatabaseName()).isEqualTo("merchant");
            assertThat(getHost()).isEqualTo("localhost");
            assertThat(getMappedPort()).isEqualTo(15432);
        }
    }

    static class MongoFixture extends AbstractMongoDBContainerTest {
        void check() {
            assertThat(getConnectionString()).isEqualTo("mongodb://localhost:27017");
            assertThat(getReplicaSetUrl()).contains("replicaSet=rs0");
            assertThat(getHost()).isEqualTo("localhost");
            assertThat(getPort()).isEqualTo(27017);
        }
    }

    static class RedisFixture extends AbstractRedisContainerTest {
        void check() {
            assertThat(getRedisHost()).isEqualTo("localhost");
            assertThat(getRedisPort()).isEqualTo(16379);
            assertThat(getRedisUrl()).isEqualTo("redis://localhost:16379");
        }
    }

    static class KafkaFixture extends AbstractKafkaContainerTest {
        void check() {
            assertThat(getBootstrapServers()).isEqualTo("localhost:19092");
            assertThat(getHost()).isEqualTo("localhost");
            assertThat(getPort()).isEqualTo(19092);
        }
    }

    static class ElasticsearchFixture extends AbstractElasticsearchContainerTest {
        void check() {
            assertThat(getHttpHostAddress()).isEqualTo("localhost:19200");
            assertThat(getHost()).isEqualTo("localhost");
            assertThat(getPort()).isEqualTo(19200);
        }
    }
}
