package io.github.loadup.retrytask.example.processor;

import io.github.loadup.retrytask.core.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyBizTaskProcessor implements RetryTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MyBizTaskProcessor.class);

    public static final String BIZ_TYPE = "EXP";

    @Override
    public boolean process(RetryTask task) {
        logger.info("Processing task: {}", task);
        // Simulate a failure
        return true;
    }

    @Override
    public String getBizType() {
        return BIZ_TYPE;
    }
}
