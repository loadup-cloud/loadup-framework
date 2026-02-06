package io.github.loadup.components.testcontainers.initializer;

/*-
 * #%L
 * Loadup Components TestContainers
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.github.loadup.components.testcontainers.listener.TestContainersExecutionListener;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Unified ApplicationContextInitializer for testcontainer properties injection.
 *
 * <p>This initializer supports two modes:
 * <ol>
 *   <li><b>Annotation mode</b>: Works with {@link io.github.loadup.components.testcontainers.annotation.EnableTestContainers}
 *       <ul>
 *         <li>TestExecutionListener starts containers and stores properties</li>
 *         <li>This initializer injects stored properties before ApplicationContext refresh</li>
 *       </ul>
 *   </li>
 *   <li><b>Configuration mode</b>: Works with application.yml configuration
 *       <ul>
 *         <li>Reads loadup.testcontainers.* properties</li>
 *         <li>Starts enabled containers and injects properties</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>Usage examples:
 * <pre>
 * // Annotation mode (recommended)
 * &#64;SpringBootTest
 * &#64;EnableTestContainers(ContainerType.MYSQL)
 * class MyTest { }
 *
 * // Configuration mode
 * &#64;SpringBootTest
 * &#64;ContextConfiguration(initializers = TestContainersPropertyInitializer.class)
 * class MyTest { }
 *
 * # application-test.yml
 * loadup:
 *   testcontainers:
 *     mysql:
 *       enabled: true
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class TestContainersPropertyInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info(">>> [TESTCONTAINERS] Initializer started");

        // Mode 1: Get properties stored by TestExecutionListener (annotation mode)
        Map<String, String> properties = TestContainersExecutionListener.getStoredProperties();

        if (properties != null && !properties.isEmpty()) {
            log.info(">>> [TESTCONTAINERS] Found {} properties from TestExecutionListener (annotation mode)",
                    properties.size());
            injectProperties(applicationContext, properties);
            return;
        }

        // Mode 2: Configuration mode - read from application.yml
        log.debug(">>> [TESTCONTAINERS] No properties from TestExecutionListener, checking configuration mode");
        properties = startContainersFromConfiguration(applicationContext);

        if (properties != null && !properties.isEmpty()) {
            log.info(">>> [TESTCONTAINERS] Started {} containers from configuration", properties.size());
            injectProperties(applicationContext, properties);
        } else {
            log.debug(">>> [TESTCONTAINERS] No containers configured, skipping initialization");
        }
    }

    /**
     * Start containers based on configuration properties.
     */
    private Map<String, String> startContainersFromConfiguration(ConfigurableApplicationContext context) {
        TestContainersProperties config = Binder.get(context.getEnvironment())
                .bind("loadup.testcontainers", TestContainersProperties.class)
                .orElse(null);

        if (config == null || !config.isEnabled()) {
            log.debug(">>> [TESTCONTAINERS] TestContainers not enabled in configuration");
            return null;
        }

        Map<String, String> allProperties = new HashMap<>();

        // MySQL
        if (config.getMysql() != null && config.getMysql().isEnabled()) {
            log.info(">>> [TESTCONTAINERS] Starting MySQL from configuration");
            SharedMySQLContainer.startContainer(config.getMysql());
            allProperties.putAll(SharedMySQLContainer.getProperties());
        }

        // PostgreSQL
        if (config.getPostgresql() != null && config.getPostgresql().isEnabled()) {
            log.info(">>> [TESTCONTAINERS] Starting PostgreSQL from configuration");
            SharedPostgreSQLContainer.startContainer(config.getPostgresql());
            allProperties.putAll(SharedPostgreSQLContainer.getProperties());
        }

        // MongoDB
        if (config.getMongodb() != null && config.getMongodb().isEnabled()) {
            log.info(">>> [TESTCONTAINERS] Starting MongoDB from configuration");
            SharedMongoDBContainer.startContainer(config.getMongodb());
            allProperties.putAll(SharedMongoDBContainer.getProperties());
        }

        // Kafka
        if (config.getKafka() != null && config.getKafka().isEnabled()) {
            log.info(">>> [TESTCONTAINERS] Starting Kafka from configuration");
            SharedKafkaContainer.startContainer(config.getKafka());
            allProperties.putAll(SharedKafkaContainer.getProperties());
        }

        // Elasticsearch
        if (config.getElasticsearch() != null && config.getElasticsearch().isEnabled()) {
            log.info(">>> [TESTCONTAINERS] Starting Elasticsearch from configuration");
            SharedElasticsearchContainer.startContainer(config.getElasticsearch());
            allProperties.putAll(SharedElasticsearchContainer.getProperties());
        }

        return allProperties;
    }

    /**
     * Inject properties into ApplicationContext environment.
     */
    private void injectProperties(ConfigurableApplicationContext context, Map<String, String> properties) {
        log.info(">>> [TESTCONTAINERS] Injecting {} properties into ApplicationContext", properties.size());

        TestPropertyValues.of(properties)
                .applyTo(context.getEnvironment());

        log.info(">>> [TESTCONTAINERS] ========== Property Injection Complete ==========");
    }
}
