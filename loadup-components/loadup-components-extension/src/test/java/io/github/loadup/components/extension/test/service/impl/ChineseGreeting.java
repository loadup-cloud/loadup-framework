package io.github.loadup.components.extension.test.service.impl;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension(bizCode = "ChineseGreeting")
public class ChineseGreeting implements GreetingService {

    @Override
    public String greet() {
        LogUtil.info(ChineseGreeting.class, "ChineseGreeting:{}", "你好");
        return "你好";
    }
}
