package com.github.loadup.components.extension.test.service.impl;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension
public class DefaultGreeting implements GreetingService {
    @Override
    public String greet() {
        return "Default";
    }
}
