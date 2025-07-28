/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test;

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.extension.exector.ExtensionExecutor.ExtensionNotFoundException;
import com.github.loadup.components.extension.test.service.GreetingService;
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
                GreetingService.class,
                BizScenario.valueOf("ChineseGreeting"),
                GreetingService::greet
        );
        String englishGreeting = extensionExecutor.execute(
                GreetingService.class,
                BizScenario.valueOf("EnglishGreeting"),
                GreetingService::greet
        );
        assertEquals("你好", chineseGreeting);
        assertEquals("Hello", englishGreeting);
    }

    @Test
    public void testExtensionUtilWithNonExistentBizCode() {
        assertThrows(
                ExtensionNotFoundException.class,
                () -> extensionExecutor.execute(
                        GreetingService.class,
                        BizScenario.valueOf("NoSuchGreeting"),
                        GreetingService::greet
                ));
    }

}
