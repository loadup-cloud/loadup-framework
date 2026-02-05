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

import io.github.loadup.components.testcontainers.initializer.TestContainersContextInitializer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.ContextConfiguration;

/**
 * Annotation to enable specific testcontainers for integration tests.
 *
 * <p>This annotation works with both JUnit 5 and TestNG tests.
 * The test class information is automatically detected by the initializer.
 *
 * <p>Usage example:
 * <pre>
 * &#64;SpringBootTest
 * &#64;EnableTestContainers(ContainerType.MYSQL, ContainerType.REDIS)
 * class MyIntegrationTest {
 *     // Your test code here
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ContextConfiguration(initializers = TestContainersContextInitializer.class)
public @interface EnableTestContainers {

    /**
     * The types of containers to enable for this test.
     * @return array of container types
     */
    ContainerType[] value() default {};

    /**
     * Whether to reuse containers across test runs.
     * @return true to reuse containers, false otherwise
     */
    boolean reuse() default true;
}
