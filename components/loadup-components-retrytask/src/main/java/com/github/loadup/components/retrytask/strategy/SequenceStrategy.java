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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Laysan
 */
@Component
public class SequenceStrategy implements RetryTaskStrategy {
    @Override
    public RetryStrategyType getStrategyType() {
        return RetryStrategyType.INTERVAL_SEQUENCE;
    }

    @Override
    public LocalDateTime calculateNextExecuteTime(RetryTask retryTask, RetryStrategyConfig retryStrategyConfig) {

        String[] intervals = StringUtils.split(retryStrategyConfig.getStrategyValue(), ",");
        int executedTimes = retryTask.getExecutedTimes();

        int intervalsIdx = (executedTimes + 1) >= intervals.length ? intervals.length - 1 : executedTimes;
        int nextInterval = Integer.parseInt(intervals[intervalsIdx]);
        return addTime(retryTask.getNextExecuteTime(), nextInterval, retryStrategyConfig.getStrategyValueUnit());
    }
}
