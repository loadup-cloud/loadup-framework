package io.github.loadup.components.extension.test.service.impl;

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension
public class DefaultGreeting implements GreetingService {
    @Override
    public String greet() {
        return "Default";
    }
}
