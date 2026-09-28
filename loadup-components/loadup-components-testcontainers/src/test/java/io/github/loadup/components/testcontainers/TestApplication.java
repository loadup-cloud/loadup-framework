package io.github.loadup.components.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 为集成测试提供 Spring Boot 上下文环境
 */
@SpringBootApplication
public class TestApplication {
    private static final Logger log = LoggerFactory.getLogger(TestApplication.class);

    // 空实现即可，它主要用于开启自动配置和属性绑定
}
