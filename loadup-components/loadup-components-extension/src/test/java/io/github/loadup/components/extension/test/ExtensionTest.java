package io.github.loadup.components.extension.test;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.loadup.components.extension.core.BizScenario;
import io.github.loadup.components.extension.exector.ExtensionExecutor;
import io.github.loadup.components.extension.exector.ExtensionExecutor.ExtensionNotFoundException;
import io.github.loadup.components.extension.test.service.GreetingService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class ExtensionTest {

    @Resource
    private ExtensionExecutor extensionExecutor; // 直接注入，自动装配完成

    @Test
    public void testExtensionRegistration() {

        String chineseGreeting = extensionExecutor.execute(
                GreetingService.class, BizScenario.valueOf("ChineseGreeting"), GreetingService::greet);
        String englishGreeting = extensionExecutor.execute(
                GreetingService.class, BizScenario.valueOf("EnglishGreeting"), GreetingService::greet);
        assertEquals("你好", chineseGreeting);
        assertEquals("Hello", englishGreeting);
    }

    @Test
    public void testExtensionUtilWithNonExistentBizCode() {
        assertThrows(
                ExtensionNotFoundException.class,
                () -> extensionExecutor.execute(
                        GreetingService.class, BizScenario.valueOf("NoSuchGreeting"), GreetingService::greet));
    }
}
