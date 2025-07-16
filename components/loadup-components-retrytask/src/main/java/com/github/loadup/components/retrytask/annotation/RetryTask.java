package com.github.loadup.components.retrytask.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryTask {
    String businessType() default "DEFAULT";

    String intervalUnit() default "MINUTES";

    int maxLoadNum() default 100;

    String strategyType() default "INTERVAL_SEQUENCE";

    String strategyValue() default "1,1,2,5,9";

    int maxRetries() default 99;

    String threadPool() default "retryTaskExecutorThreadPool";
}