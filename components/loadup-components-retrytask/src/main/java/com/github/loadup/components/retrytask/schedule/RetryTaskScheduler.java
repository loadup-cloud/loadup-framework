package com.github.loadup.components.retrytask.schedule;

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.registry.TaskStrategyRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RetryTaskScheduler {

    @Resource
    private RetryTaskLoader        retryTaskLoader;
    @Resource
    private ThreadPoolTaskExecutor retryTaskExecutorThreadPool;
    @Resource
    private ThreadPoolTaskExecutor retryTaskLoaderThreadPool;
    @Resource
    private RetryTaskExecuter     retryTaskExecuter;
    @Resource
    private TaskStrategyRegistry  taskStrategyRegistry;
    @Autowired
    private JdbcCustomConversions conversions;

    @PostConstruct
    public void checkConversions() {
        System.out.println("Registered converters: " + conversions.getPropertyValueConversions());
    }

    /**
     * trigger retry task by scheduled
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void triggerRetryTask() {

        long startTime = System.currentTimeMillis();
        log.info("RetryTaskScheduler begin retry.");
        try {
            Map<String, RetryStrategyConfig> retryStrategyConfigMap = taskStrategyRegistry.getTaskStrategyConfigMap();
            for (String bizType : retryStrategyConfigMap.keySet()) {
                // load by bizType async
                retryTaskLoaderThreadPool.execute(() -> {
                    List<String> businessKeys = retryTaskLoader.load(bizType);
                    for (final String businessKey : businessKeys) {
                        // execute async
                        retryTaskExecutorThreadPool.execute(() -> retryTaskExecuter.execute(businessKey));
                    }
                });
            }

        } finally {
            long elapseTime = System.currentTimeMillis() - startTime;
            log.info("RetryTaskScheduler finish retry, elapseTime=" + elapseTime);
        }
    }
}
