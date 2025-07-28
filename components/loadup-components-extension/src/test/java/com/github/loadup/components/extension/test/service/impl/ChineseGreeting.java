/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test.service.impl;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.test.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Extension(bizCode = "ChineseGreeting")
public class ChineseGreeting implements GreetingService {
    @Override
    public String greet() {
        log.info("ChineseGreeting:{}", "你好");
        return "你好";
    }
}
