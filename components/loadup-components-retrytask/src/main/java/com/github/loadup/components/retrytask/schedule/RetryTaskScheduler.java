package com.github.loadup.components.retrytask.schedule;

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.factory.RetryStrategyFactory;
import com.github.loadup.components.retrytask.factory.TransactionTemplateFactory;
import com.github.loadup.components.retrytask.manager.RetryTaskExecutor;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

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
    private RetryTaskExecutor          retryTaskExecutor;
    @Resource
    private RetryStrategyFactory       taskStrategyFactory;
    @Autowired
    private JdbcCustomConversions conversions;
    @Autowired
    private RetryTaskRepository        retryTaskRepository;
    @Autowired
    private TransactionTemplateFactory transactionTemplateFactory;

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
            Map<String, RetryStrategyConfig> retryStrategyConfigMap = taskStrategyFactory.getTaskStrategyConfigMap();
            // load by bizType async
            //retryStrategyConfigMap.keySet().forEach(bizType -> retryTaskLoaderThreadPool.execute(() -> {
            //    List<String> businessKeys = retryTaskLoader.load(bizType);
            //    // execute async
            //    businessKeys.forEach(businessKey -> retryTaskExecutorThreadPool.execute(() -> retryTaskExecuter.execute(businessKey)));
            //}));
            retryStrategyConfigMap.forEach((bizType, retryStrategyConfig) -> {
                List<RetryTaskDO> retryTaskList = retryTaskLoader.loadTasks(bizType);
                for (RetryTaskDO retryTask : retryTaskList) {
                    if (retryStrategyConfig.runInTransaction()) {
                        TransactionTemplate transactionTemplate = this.transactionTemplateFactory.obtainTemplate(bizType);
                        RetryTaskDO finalRetryTask = retryTask;
                        RetryTaskDO task = transactionTemplate.execute(
                                status -> RetryTaskScheduler.this.doBiz(bizType, finalRetryTask.getBusinessId(), true));
                        possessAfter(task);
                    } else {
                        RetryTaskDO task = this.doBiz(bizType, retryTask.getBusinessId(), false);
                        possessAfter(task);
                    }
                }
            });

        } catch (Exception e) {
            log.error("RetryTaskScheduler run failed");
        } finally {
            long elapseTime = System.currentTimeMillis() - startTime;
            log.info("RetryTaskScheduler finish retry, elapseTime={}", elapseTime);
        }
    }

    private void possessAfter(RetryTaskDO task) {

    }

    private RetryTaskDO doBiz(String bizType, String businessId, boolean runInTransaction) {
        RetryTaskDO retryTask = null;
        try {
            if (runInTransaction) {
                retryTask = retryTaskRepository.lockByBizId(businessId, bizType);
            } else {
                retryTask = retryTaskRepository.findByBizId(businessId, bizType);
            }

            if (retryTask == null) {
                log.warn("the task is not existed ,bizId={}, bizType={} ", businessId, bizType);
            } else {
                retryTaskExecutor.plainExecute(retryTask);
            }
            return null;
        } catch (Exception e) {
            log.warn("the task executed failed  ,bizId={}, bizType={},ex={} ", businessId, bizType, e);
            return retryTask;
        }
    }
}
