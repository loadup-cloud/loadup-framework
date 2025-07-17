package com.github.loadup.components.retrytask.strategy;

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

import com.github.loadup.components.retrytask.enums.RetryStrategyType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ExponentialBackoffInterval implements RetryTaskInterval {
    /**
     * 1 unit 作为指数
     */
    private long initialInterval = 1;
    /**
     * 最大重试到 50 unit
     */
    private long maxInterval     = 50;

    public ExponentialBackoffInterval(long initialMillis, long maxMillis) {
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
    }

    @Override
    public String getStrategyType() {
        return RetryStrategyType.EXPONENTIAL_WAIT_STRATEGY;
    }

    /**
     * 创建一个永久重试的重试器，每次重试失败时以递增的指数时间等待，直到最多5分钟。
     * 5分钟后，每隔5分钟重试一次。
     * 第一次失败后，依次等待时长：2^1 * 100;2^2 * 100；2^3 * 100;...
     * strategyValue 配置为multiplier，maximumWait 不填或填错则使用默认值
     */
    @Override
    public LocalDateTime calculateNextRetryTime(int retryCount) {
        long delay = (long) Math.min(initialInterval * Math.pow(2, retryCount), maxInterval);
        return LocalDateTime.now().plus(delay, ChronoUnit.MILLIS);
    }
}
