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
package io.github.loadup.components.testcontainers.annotation;

import io.github.loadup.components.testcontainers.initializer.TestContainersPropertyInitializer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ContextConfiguration(initializers = TestContainersPropertyInitializer.class)
public @interface EnableTestContainers {

    /**
     * The types of containers to enable for this test.
     *
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
