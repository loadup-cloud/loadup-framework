package io.github.loadup.components.extension.test.service.impl;

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.test.service.GreetingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Extension(bizCode = "EnglishGreeting")
public class EnglishGreeting implements GreetingService {
    private static final Logger log = LoggerFactory.getLogger(EnglishGreeting.class);

    @Override
    public String greet() {
        log.info("EnglishGreeting:{}", "Hello");
        return "Hello";
    }
}
