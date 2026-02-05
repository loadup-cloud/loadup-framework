package io.github.loadup.components.testcontainers.annotation;

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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable specific testcontainers for integration tests.
 *
 * <p>This annotation is automatically detected by {@link io.github.loadup.components.testcontainers.listener.TestContainersExecutionListener}
 * which is registered via spring.factories. No additional configuration is needed.
 *
 * <p>Works with both JUnit 5 and TestNG tests.
 *
 * <p>Usage example:
 * <pre>
 * &#64;SpringBootTest
 * &#64;EnableTestContainers(ContainerType.MYSQL)
 * class MyIntegrationTest {
 *     // MySQL container automatically started before test class initialization
 *     // Connection properties automatically injected into Spring environment
 * }
 * </pre>
 *
 * <p>Multiple containers:
 * <pre>
 * &#64;EnableTestContainers({ContainerType.MYSQL, ContainerType.REDIS, ContainerType.KAFKA})
 * class IntegrationTest {
 *     // All three containers started
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableTestContainers {

    /**
     * The types of containers to enable for this test.
     * @return array of container types
     */
    ContainerType[] value() default {};

    /**
     * Whether to reuse containers across test runs.
     * When enabled, containers will not be stopped after tests complete,
     * allowing subsequent test runs to reuse the same container instances.
     *
     * <p>Note: Container reuse requires Testcontainers to be configured with
     * reuse enabled in ~/.testcontainers.properties:
     * <pre>
     * testcontainers.reuse.enable=true
     * </pre>
     *
     * @return true to reuse containers (default), false to stop after each test
     */
    boolean reuse() default true;
}
