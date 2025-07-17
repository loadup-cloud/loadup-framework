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

public class FibonacciWaitInterval implements RetryTaskInterval {
    /**
     * 1 unit 作为指数
     */
    private long initialInterval = 1;
    /**
     * 最大重试到 50 unit
     */
    private long maxInterval     = 50;

    public FibonacciWaitInterval(long initialInterval, long maxInterval) {
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
    }

    @Override
    public String getStrategyType() {
        return RetryStrategyType.FIBONACCI_WAIT_STRATEGY;
    }

    /**
     * 创建一个永久重试的重试器，每次重试失败时以斐波那契数列来计算等待时间，直到最多5分钟；5分钟后，每隔5分钟重试一次；
     * <p>
     * 第一次失败后，依次等待时长：1*1;1*1；2*1；3*1；5*1；...
     * strategyValue 配置为multiplier，maxInterval 不填或填错则使用默认值
     */
    @Override
    public LocalDateTime calculateNextRetryTime(int retryCount) {

        long fib = fib(retryCount);
        long delay = initialInterval * fib;

        if (delay > maxInterval || delay < 0L) {
            delay = maxInterval;
        }
        return LocalDateTime.now().plus(delay, ChronoUnit.MILLIS);
    }

    private long fib(long n) {
        if (n == 0L) {
            return 0L;
        }
        if (n == 1L) {
            return 1L;
        }

        long prevPrev = 0L;
        long prev = 1L;
        long result = 0L;

        for (long i = 2L; i <= n; i++) {
            result = prev + prevPrev;
            prevPrev = prev;
            prev = result;
        }

        return result;
    }
}
