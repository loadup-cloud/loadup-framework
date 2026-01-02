package com.github.loadup.components.extension.register;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;
import com.github.loadup.components.extension.core.BizScenario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/** Tests for ExtensionRegistry caching functionality */
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
    List<ExtensionCoordinate> extensions1 =
        registry.getExtensionsByScenario(TestExtension.class, scenario);
    assertNotNull(extensions1);
    assertFalse(extensions1.isEmpty());

    // Second call - should use cached result
    List<ExtensionCoordinate> extensions2 =
        registry.getExtensionsByScenario(TestExtension.class, scenario);
    assertNotNull(extensions2);
    assertEquals(extensions1.size(), extensions2.size());
  }

  @Test
  void testCacheWithDifferentScenarios() {
    BizScenario scenario1 = BizScenario.valueOf("test", "default", "default");
    BizScenario scenario2 = BizScenario.valueOf("test", "special", "default");

    List<ExtensionCoordinate> extensions1 =
        registry.getExtensionsByScenario(TestExtension.class, scenario1);
    List<ExtensionCoordinate> extensions2 =
        registry.getExtensionsByScenario(TestExtension.class, scenario2);

    assertNotEquals(extensions1, extensions2, "Different scenarios should return different results");
  }

  @Test
  void testPrewarmCache() {
    // After initialization, cache should already be prewarmed
    BizScenario scenario = BizScenario.valueOf("test", "default", "default");
    
    // This call should use the prewarmed cache
    List<ExtensionCoordinate> extensions =
        registry.getExtensionsByScenario(TestExtension.class, scenario);
    
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
