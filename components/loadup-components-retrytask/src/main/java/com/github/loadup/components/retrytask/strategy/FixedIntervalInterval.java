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

public class FixedIntervalInterval implements RetryTaskInterval {
    private final long intervalMillis;

    public FixedIntervalInterval(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    @Override
    public String getStrategyType() {
        return RetryStrategyType.FIXED_WAIT_STRATEGY;
    }

    /**
     * 固定时长等待策略，失败后，将等待固定的时长进行重试；
     */
    @Override
    public LocalDateTime calculateNextRetryTime(int retryCount) {
        return LocalDateTime.now().plus(intervalMillis, ChronoUnit.MILLIS);
    }
}
