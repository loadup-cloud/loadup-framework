package io.github.loadup.components.extension.register;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.api.IExtensionPoint;
import io.github.loadup.components.extension.core.BizScenario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Tests for ExtensionRegistry caching functionality
 */
class ExtensionRegistryCacheTest {

    private ExtensionRegistry registry;
    private ApplicationContext applicationContext;

    interface TestExtension extends IExtensionPoint {}

    @Extension(bizCode = "test", useCase = "default", scenario = "default")
    static class TestExtensionImpl implements TestExtension {}

    @Extension(bizCode = "test", useCase = "special", scenario = "default")
    static class SpecialTestExtensionImpl implements TestExtension {}

    @Configuration
    static class TestConfig {
        @Bean
        public TestExtensionImpl testExtension() {
            return new TestExtensionImpl();
        }

        @Bean
        public SpecialTestExtensionImpl specialTestExtension() {
            return new SpecialTestExtensionImpl();
        }
    }

    @BeforeEach
    void setUp() {
        registry = new ExtensionRegistry();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TestConfig.class);
        context.refresh();
        applicationContext = context;

        // Trigger extension registration
        registry.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
    }

    @Test
    void testExtensionCaching() {
        BizScenario scenario = BizScenario.valueOf("test", "default", "default");

        // First call - should populate cache
        List<ExtensionCoordinate> extensions1 = registry.getExtensionsByScenario(TestExtension.class, scenario);
        assertNotNull(extensions1);
        assertFalse(extensions1.isEmpty());

        // Second call - should use cached result
        List<ExtensionCoordinate> extensions2 = registry.getExtensionsByScenario(TestExtension.class, scenario);
        assertNotNull(extensions2);
        assertEquals(extensions1.size(), extensions2.size());
    }

    @Test
    void testCacheWithDifferentScenarios() {
        BizScenario scenario1 = BizScenario.valueOf("test", "default", "default");
        BizScenario scenario2 = BizScenario.valueOf("test", "special", "default");

        List<ExtensionCoordinate> extensions1 = registry.getExtensionsByScenario(TestExtension.class, scenario1);
        List<ExtensionCoordinate> extensions2 = registry.getExtensionsByScenario(TestExtension.class, scenario2);

        assertNotEquals(extensions1, extensions2, "Different scenarios should return different results");
    }

    @Test
    void testPrewarmCache() {
        // After initialization, cache should already be prewarmed
        BizScenario scenario = BizScenario.valueOf("test", "default", "default");

        // This call should use the prewarmed cache
        List<ExtensionCoordinate> extensions = registry.getExtensionsByScenario(TestExtension.class, scenario);

        assertNotNull(extensions);
        assertFalse(extensions.isEmpty());
    }

    @Test
    void testGetAllExtensionTypes() {
        var types = registry.getAllExtensionTypes();
        assertNotNull(types);
        assertTrue(types.contains(TestExtension.class));
    }

    @Test
    void testGetAllBizCodes() {
        var bizCodes = registry.getAllBizCodes();
        assertNotNull(bizCodes);
        assertTrue(bizCodes.contains("test"));
    }
}
