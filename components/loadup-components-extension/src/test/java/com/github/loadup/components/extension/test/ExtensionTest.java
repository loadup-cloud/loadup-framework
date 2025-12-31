package com.github.loadup.components.extension.test;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.extension.exector.ExtensionExecutor.ExtensionNotFoundException;
import com.github.loadup.components.extension.test.service.GreetingService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class ExtensionTest {

  @Resource private ExtensionExecutor extensionExecutor; // 直接注入，自动装配完成

  @Test
  public void testExtensionRegistration() {

    String chineseGreeting =
        extensionExecutor.execute(
            GreetingService.class, BizScenario.valueOf("ChineseGreeting"), GreetingService::greet);
    String englishGreeting =
        extensionExecutor.execute(
            GreetingService.class, BizScenario.valueOf("EnglishGreeting"), GreetingService::greet);
    assertEquals("你好", chineseGreeting);
    assertEquals("Hello", englishGreeting);
  }

  @Test
  public void testExtensionUtilWithNonExistentBizCode() {
    assertThrows(
        ExtensionNotFoundException.class,
        () ->
            extensionExecutor.execute(
                GreetingService.class,
                BizScenario.valueOf("NoSuchGreeting"),
                GreetingService::greet));
  }
}
