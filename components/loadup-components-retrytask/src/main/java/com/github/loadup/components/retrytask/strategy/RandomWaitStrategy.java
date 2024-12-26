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

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.enums.RetryStrategyType;
import com.github.loadup.components.retrytask.model.RetryTask;

import java.time.LocalDateTime;
import java.util.Date;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RandomWaitStrategy implements RetryTaskStrategy {
    /**
     * minimum
     */
    private long minimum = 1;
    /**
     * maximum
     */
    private long maximum = 10;

    @Override
    public RetryStrategyType getStrategyType() {
        return RetryStrategyType.RANDOM_WAIT_STRATEGY;
    }

    /**
     * 随机时长等待策略，可以设置一个随机等待的最大时长，也可以设置一个随机等待的时长区间。
     */

    @Override
    public LocalDateTime calculateNextExecuteTime(RetryTask retryTask, RetryStrategyConfig retryStrategyConfig) {
        String[] intervals = StringUtils.split(retryStrategyConfig.getStrategyValue(), ",");
        if (intervals.length == 1) {
            maximum = Long.parseLong(intervals[0]);
        } else if (intervals.length == 2) {
            minimum = Long.parseLong(intervals[0]);
            maximum = Long.parseLong(intervals[1]);
        }
        long result = Math.abs(RandomUtils.nextLong()) % (maximum - minimum);
        return addTime(retryTask.getNextExecuteTime(), Math.toIntExact(result + minimum), retryStrategyConfig.getStrategyValueUnit());
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {

            System.out.println(RandomUtils.nextLong() % 10);
        }
    }

}
