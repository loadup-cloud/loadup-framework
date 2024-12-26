//package com.github.loadup.components.retrytask.schedule;
//
/// *-
// * #%L
// * loadup-components-retrytask
// * %%
// * Copyright (C) 2022 - 2023 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
//import com.github.loadup.components.retrytask.config.RetryTaskFactory;
//import java.util.List;
//import java.util.Map;
//import javax.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//
/// **
// * RetryTaskScheduler
// */
//@Component
//@Slf4j
//public class RetryTaskScheduler {
//
//    @Resource
//    private RetryTaskLoader        retryTaskLoader;
//    @Resource
//    private ThreadPoolTaskExecutor retryTaskExecutorThreadPool;
//    @Resource
//    private ThreadPoolTaskExecutor retryTaskLoaderThreadPool;
//    @Resource
//    private RetryTaskExecuter      retryTaskExecuter;
//    @Autowired
//    private RetryTaskFactory       retryTaskFactory;
//
//    /**
//     * trigger retry task by scheduled
//     */
//    @Async
//    @Scheduled(cron = "0/10 * * * * ?")
//    public void triggerRetryTask() {
//
//        long startTime = System.currentTimeMillis();
//        //log.info("RetryTaskScheduler begin retry.");
//        try {
//            Map<String, RetryStrategyConfig> retryStrategyConfigMap = retryTaskFactory.getRetryStrategyConfigs();
//            for (String bizType : retryStrategyConfigMap.keySet()) {
//                // load by bizType async
//                retryTaskLoaderThreadPool.execute(() -> {
//                    List<String> businessKeys = retryTaskLoader.load(bizType);
//                    for (final String businessKey : businessKeys) {
//                        // execute async
//                        retryTaskExecutorThreadPool.execute(() -> retryTaskExecuter.execute(businessKey));
//                    }
//                });
//            }
//
//        } finally {
//            //long elapseTime = System.currentTimeMillis() - startTime;
//            //log.info("RetryTaskScheduler finish retry, elapseTime=" + elapseTime);
//        }
//    }
//}
