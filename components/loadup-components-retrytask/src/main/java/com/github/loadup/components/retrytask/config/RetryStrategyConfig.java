package com.github.loadup.components.retrytask.config;

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

import com.github.loadup.components.retrytask.annotation.RetryTask;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * The config of retry task strategy
 */
@Getter
@Setter
public class RetryStrategyConfig {

    /**
     * business type, users can define themselves
     * <p>
     * DEFAULT(in default)
     */
    private String businessType;

    /**
     * the max retry task sum in every schedule. If user define more than one retry strategy , the sum of loading task in shedule is the
     * minimum one
     */
    private int maxLoadNum;

    /**
     * the type of retry strategy
     * INTERVAL_SEQUENCE(in default)
     */
    private String strategyType;

    /**
     * the value of retry strategy
     * format ?,?,?,?...
     * 1,1,2,5,9(in default).The unit is by minute, For example:"1,1,2,5,9"，
     * the interval between every retry task is the last digital of the retry strategy value
     */
    private String strategyValue;

    /**
     * the value unit of retry strategy
     * S(SECOND),I(MINUTE),H(HOUR),D(DAY)
     * default is MINUTE
     */
    private String intervalUnit;

    /**
     * the maximum of the sum of execution
     * -1 is infinite
     */
    private int maxRetries;

    /**
     * extreme retryTime, used to load task when task.processingFlag ;
     * for example: when RetryTaskExecutor.beforeExecute update processingFlag = 'T', lately update processing = 'F' failed,
     * then the task.processing will be 'T' in DB. some system want to retry quickly, so we provided this params.
     */
    private int extremeRetryTime;

    /**
     * status, true is open. false is closed
     */
    private String status;

    /**
     * is execute immediately
     */
    private boolean isExecuteImmediately;

    /**
     * is ignore priority
     */
    private boolean isIgnorePriority;

    /**
     * 这个表示单次任务执行时间限制（如果单次任务执行超时，则终止执行当前任务）；
     */
    private String attemptTimeLimiter;

    private String threadPool;

    public static RetryStrategyConfig of(RetryTask task) {
        RetryStrategyConfig config = new RetryStrategyConfig();
        config.setBusinessType(task.businessType());
        config.setStrategyType(task.strategyType());
        config.setStrategyValue(task.strategyValue());
        config.setMaxLoadNum(task.maxLoadNum());
        config.setMaxRetries(task.maxRetries());
        config.setIntervalUnit(task.intervalUnit());
        config.setThreadPool(task.threadPool());
        return config;
    }

    public void mergeFrom(RetryStrategyConfig task) {
        if (StringUtils.isNotBlank(task.getBusinessType())) {
            this.setBusinessType(task.getBusinessType());
        }
        if (StringUtils.isNotBlank(task.getStrategyType())) {
            this.setStrategyType(task.getStrategyType());
        }
        if (StringUtils.isNotBlank(task.getStrategyValue())) {
            this.setStrategyValue(task.getStrategyValue());
        }
        if (Objects.nonNull(task.getMaxLoadNum()) && task.getMaxLoadNum() > 0) {
            this.setMaxLoadNum(task.getMaxLoadNum());
        }
        if (Objects.nonNull(task.getMaxRetries()) && task.getMaxRetries() > 0) {
            this.setMaxRetries(task.getMaxRetries());
        }
        if (StringUtils.isNotBlank(task.getIntervalUnit())) {
            this.setIntervalUnit(task.getIntervalUnit());
        }
        if (StringUtils.isNotBlank(task.getThreadPool())) {
            this.setThreadPool(task.getThreadPool());
        }
    }
}
