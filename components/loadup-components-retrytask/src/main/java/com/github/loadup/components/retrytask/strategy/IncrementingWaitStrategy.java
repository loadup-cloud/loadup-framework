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
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
public class IncrementingWaitStrategy implements RetryTaskStrategy {
    private long initialSleepTime = 1;
    private long increment        = 5;

    @Override
    public String getStrategyType() {
        return RetryStrategyType.INCREMENTING_WAIT_STRATEGY;
    }

    /**
     * 递增等待策略，根据初始值和递增值，等待时长依次递增。就本例而言：
     * 第一次失败后，将依次等待1s；6s(1+5)；11(1+5+5)s；16(1+5+5+5)s；...
     */
    @Override
    public LocalDateTime calculateNextRetryTime(int retryCount) {

        long delay = initialSleepTime + (increment * (retryCount - 1));
        return LocalDateTime.now().plus(delay, ChronoUnit.MILLIS);
    }
}
