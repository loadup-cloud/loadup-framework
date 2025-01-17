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

import com.github.loadup.commons.enums.TimeUnitEnum;
import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
import com.github.loadup.components.retrytask.enums.RetryStrategyType;
import com.github.loadup.components.retrytask.spi.RetryTaskExecutorSpi;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * The config of retry task strategy
 */
@Getter
@Setter
@Component
public class RetryStrategyConfig {

    /**
     * business type, users can define themselves
     * <p>
     * DEFAULT(in default)
     */
    private String bizType = RetryTaskConstants.DEFAULT_BIZ_TYPE;

    /**
     * the max retry task sum in every schedule. If user define more than one retry strategy , the sum of loading task in shedule is the
     * minimum one
     */
    private int maxLoadNum = 1000;

    /**
     * the type of retry strategy
     * INTERVAL_SEQUENCE(in default)
     */
    private String strategyType = RetryStrategyType.INTERVAL_SEQUENCE.getCode();

    /**
     * the value of retry strategy
     * format ?,?,?,?...
     * 1,1,2,5,9(in default).The unit is by minute, For example:"1,1,2,5,9"，
     * the interval between every retry task is the last digital of the retry strategy value
     */
    private String strategyValue = "1,1,2,5,9";

    /**
     * the value unit of retry strategy
     * S(SECOND),I(MINUTE),H(HOUR),D(DAY)
     * default is MINUTE
     */
    private String strategyValueUnit = TimeUnitEnum.MINUTE.getCode();

    /**
     * the maximum of the sum of execution
     * -1 is infinite
     */
    private int maxExecuteCount = -1;

    /**
     * extreme retryTime, used to load task when task.processingFlag = T, the unit is minute </br>
     * for example: when RetryTaskExecutor.beforeExecute update processingFlag = 'T', lately update processing = 'F' failed,
     * then the task.processing will be 'T' in DB. some system want to retry quickly, so we provided this params.
     */
    private int extremeRetryTime = 1;

    /**
     * status, true is open. false is closed
     */
    private String status;

    /**
     * is execute immediately
     */
    private boolean isExecuteImmediately = true;

    /**
     * is ignore priority
     */
    private boolean isIgnorePriority = true;

    /**
     * 这个表示单次任务执行时间限制（如果单次任务执行超时，则终止执行当前任务）；
     */
    private String attemptTimeLimiter;

    private RetryTaskExecutorSpi executor;

}
