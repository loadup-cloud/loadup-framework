/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test.service.impl;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension(bizCode = "EnglishGreeting")
public class EnglishGreeting implements GreetingService {
    @Override
    public String greet() {
        return "Hello";
    }
}
