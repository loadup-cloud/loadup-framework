/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test;

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.extension.ExtensionRegistry;
import com.github.loadup.components.extension.test.service.GreetingService;
import com.github.loadup.components.extension.util.ExtensionUtil;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class ExtensionUtilTest {

    @Autowired
    private ExtensionRegistry extensionRegistry;

    @BeforeEach
    void setUp() {
        // 确保 registry 已经被 ExtensionRegistrar 初始化
    }

    @Test
    void testGetExtensionByBizCode() {
        GreetingService chinese = ExtensionUtil.getExtension(GreetingService.class, "ChineseGreeting");
        GreetingService english = ExtensionUtil.getExtension(GreetingService.class, "EnglishGreeting");

        assertNotNull(chinese);
        assertNotNull(english);

        assertEquals("你好", chinese.greet());
        assertEquals("Hello", english.greet());
    }

    @Test
    void testGetNonExistentExtensionShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtensionUtil.getExtension(GreetingService.class, "NoSuchGreeting"));
    }

    @Test
    void testGetAllExtensions() {
        Map<String, Object> extensions = ExtensionUtil.getAllExtensions();

        assertTrue(extensions.containsKey("ChineseGreeting"));
        assertTrue(extensions.containsKey("EnglishGreeting"));

        assertEquals(3, extensions.size());
    }

    @Test
    void testDefaultBizCodeMatchesInterfaceName() {
        String defaultBizCode = GreetingService.class.getName();
        Object registered = extensionRegistry.getExtension(defaultBizCode);

        assertNotNull(registered);
        assertEquals(GreetingService.class, registered.getClass().getInterfaces()[0]);
    }
}
