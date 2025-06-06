package com.github.loadup.components.extension.test.service.impl;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension(bizCode = "ChineseGreeting")
public class ChineseGreeting implements GreetingService {
    @Override
    public String greet() {
        return "你好";
    }
}
