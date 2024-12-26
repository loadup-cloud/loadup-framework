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

import com.github.loadup.commons.enums.TimeUnitEnum;
import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.enums.RetryStrategyType;
import com.github.loadup.components.retrytask.model.RetryTask;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public interface RetryTaskStrategy {
    RetryStrategyType getStrategyType();

    LocalDateTime calculateNextExecuteTime(RetryTask retryTask, RetryStrategyConfig retryStrategyConfig);

    default LocalDateTime addTime(LocalDateTime date, int interval, String unit) {
        if (StringUtils.equals(unit, TimeUnitEnum.SECOND.getCode())) {
            return date.plusSeconds(interval);
        } else if (StringUtils.equals(unit, TimeUnitEnum.MINUTE.getCode())) {
            return date.plusMinutes(interval);
        } else if (StringUtils.equals(unit, TimeUnitEnum.HOUR.getCode())) {
            return date.plusHours(interval);
        } else if (StringUtils.equals(unit, TimeUnitEnum.DAY.getCode())) {
            return date.plusDays(interval);
        } else if (StringUtils.equals(unit, TimeUnitEnum.MONTH.getCode())) {
            return date.plusMonths(interval);
        } else if (StringUtils.equals(unit, TimeUnitEnum.YEAR.getCode())) {
            return date.plusYears(interval);
        }
        return date;
    }
}
