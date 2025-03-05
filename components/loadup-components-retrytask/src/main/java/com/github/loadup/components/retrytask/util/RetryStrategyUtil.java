package com.github.loadup.components.retrytask.util;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.strategy.RetryTaskStrategy;
import com.github.loadup.components.retrytask.strategy.RetryTaskStrategyFactory;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * 重试策略工具类
 */
@Component
public class RetryStrategyUtil {
    /**
     * 分隔符
     */
    public static final char SEPARATOR = ',';

    private static RetryStrategyUtil instance;
    //    @Resource
    private RetryTaskStrategyFactory retryTaskStrategyFactory;

    /**
     * 根据重试策略更新重试任务
     */
    public static void updateRetryTaskByStrategy(RetryTask retryTask, RetryStrategyConfig retryStrategyConfig) {
        RetryTaskStrategy retryTaskStrategy =
                instance.retryTaskStrategyFactory.findRetryTaskStrategy(retryStrategyConfig.getStrategyType());
        int executedTimes = retryTask.getExecutedTimes();
        retryTask.setExecutedTimes(executedTimes + 1);
        //        Date nextExecuteTime = retryTaskStrategy.calculateNextExecuteTime(retryTask, retryStrategyConfig);
        //        retryTask.setNextExecuteTime(nextExecuteTime);
        retryTask.setModifiedTime(LocalDateTime.now());
        retryTask.setProcessing(false);
    }

    /**
     * 判断重试次数是否达到上限
     */
    private static boolean isReachMaxExecuteTimes(int executedTimes, int maxExecuteTimes) {

        if (maxExecuteTimes < 0) {
            return false;
        }
        return (executedTimes >= maxExecuteTimes);
    }

    //    @PostConstruct
    public void init() {
        instance = this;
    }
}
