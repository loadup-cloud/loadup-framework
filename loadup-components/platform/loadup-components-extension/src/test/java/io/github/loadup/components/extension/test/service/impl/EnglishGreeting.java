package io.github.loadup.components.extension.test.service.impl;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension(bizCode = "EnglishGreeting")
public class EnglishGreeting implements GreetingService {

    @Override
    public String greet() {
        LogUtil.info(EnglishGreeting.class, "EnglishGreeting:{}", "Hello");
        return "Hello";
    }
}
