/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test;

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.extension.ExtensionRegistry;
import com.github.loadup.components.extension.test.service.GreetingService;
import com.github.loadup.components.extension.util.ExtensionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class ExtensionTest {

    @Autowired
    private ExtensionRegistry extensionRegistry;

    @Test
    public void testExtensionRegistration() {
        assertNotNull(extensionRegistry.getExtension("ChineseGreeting"));
        assertNotNull(extensionRegistry.getExtension("EnglishGreeting"));

        assertEquals("你好", ((GreetingService) extensionRegistry.getExtension("ChineseGreeting")).greet());
        assertEquals("Hello", ((GreetingService) extensionRegistry.getExtension("EnglishGreeting")).greet());
    }

    @Test
    public void testExtensionUtilGetByBizCode() {
        GreetingService chinese = ExtensionUtil.getExtension(GreetingService.class, "ChineseGreeting");
        GreetingService english = ExtensionUtil.getExtension(GreetingService.class, "EnglishGreeting");

        assertNotNull(chinese);
        assertNotNull(english);

        assertEquals("你好", chinese.greet());
        assertEquals("Hello", english.greet());
    }

    @Test
    public void testExtensionUtilWithNonExistentBizCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtensionUtil.getExtension(GreetingService.class, "NoSuchGreeting"));
    }

    @Test
    public void testDefaultBizCodeUsesInterfaceName() {
        String defaultBizCode = GreetingService.class.getName();
        Object registered = extensionRegistry.getExtension(defaultBizCode);

        assertNotNull(registered);
        assertEquals(GreetingService.class, registered.getClass().getInterfaces()[0]);
        assertEquals("Default", ((GreetingService) registered).greet());
    }
}
