package com.github.loadup.components.retrytask.registry;

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
import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.enums.RetryStrategyType;
import com.github.loadup.components.retrytask.properties.RetryTaskProperties;
import com.github.loadup.components.retrytask.strategy.*;
import com.github.loadup.components.retrytask.util.TimeUnitUtils;
import jakarta.annotation.Resource;
import lombok.Getter;

import java.util.*;

@Getter
public class TaskStrategyRegistry {

    private Map<String, RetryTaskStrategy>   retryTaskStrategyMap  = new HashMap<>();
    private Map<String, RetryStrategyConfig> taskStrategyConfigMap = new HashMap<>();
    @Resource
    private RetryTaskProperties              retryTaskProperties;

    public RetryStrategyConfig getStrategyConfig(String businessType) {
        RetryStrategyConfig retryTaskStrategy = taskStrategyConfigMap.get(businessType);
        if (Objects.isNull(retryTaskStrategy)) {
            throw new IllegalArgumentException("No config found for businessType: " + businessType);
        }
        return retryTaskStrategy;
    }

    public RetryTaskStrategy getTaskStrategy(String retryStrategyType) {
        RetryTaskStrategy retryTaskStrategy = retryTaskStrategyMap.get(retryStrategyType);
        if (Objects.isNull(retryTaskStrategy)) {
            throw new IllegalArgumentException("No strategy found for taskType: " + retryStrategyType);
        }
        return retryTaskStrategy;
    }

    public void register(RetryTask annotation) {
        String strategyValue = annotation.strategyValue();
        String strategyType = annotation.strategyType();
        String intervalUnit = annotation.intervalUnit();
        String businessType = annotation.businessType();
        String[] values = strategyValue.split(",");

        switch (strategyType.toUpperCase()) {
            case RetryStrategyType.FIXED_WAIT_STRATEGY:
                long interval = TimeUnitUtils.toMillis(Long.parseLong(values[0]), intervalUnit);
                retryTaskStrategyMap.put(strategyType, new FixedIntervalStrategy(interval));
                break;
            case RetryStrategyType.EXPONENTIAL_WAIT_STRATEGY:
                long initial = TimeUnitUtils.toMillis(Long.parseLong(values[0]), intervalUnit);
                long max = TimeUnitUtils.toMillis(Long.parseLong(values[1]), intervalUnit);
                retryTaskStrategyMap.put(strategyType, new ExponentialBackoffStrategy(initial, max));
                break;
            case RetryStrategyType.RANDOM_WAIT_STRATEGY:
                long min = TimeUnitUtils.toMillis(Long.parseLong(values[0]), intervalUnit);
                long maxRand = TimeUnitUtils.toMillis(Long.parseLong(values[1]), intervalUnit);
                retryTaskStrategyMap.put(strategyType, new RandomWaitStrategy(min, maxRand));
                break;
            case RetryStrategyType.INTERVAL_SEQUENCE_STRATEGY:
                long[] intervals = TimeUnitUtils.toMillisArray(values, intervalUnit);
                retryTaskStrategyMap.put(strategyType, new IntervalSequenceStrategy(intervals));
                break;
            case RetryStrategyType.FIBONACCI_WAIT_STRATEGY:
                long initInterval = TimeUnitUtils.toMillis(Long.parseLong(values[0]), intervalUnit);
                long maxInterval = TimeUnitUtils.toMillis(Long.parseLong(values[1]), intervalUnit);
                retryTaskStrategyMap.put(strategyType, new FibonacciWaitStrategy(initInterval, maxInterval));
                break;
            case RetryStrategyType.INCREMENTING_WAIT_STRATEGY:
                long initialSleepTime = TimeUnitUtils.toMillis(Long.parseLong(values[0]), intervalUnit);
                long increment = TimeUnitUtils.toMillis(Long.parseLong(values[1]), intervalUnit);
                retryTaskStrategyMap.put(strategyType, new IncrementingWaitStrategy(initialSleepTime, increment));
                break;
            default:
                throw new IllegalArgumentException("Unsupported strategy type: " + strategyType);
        }
        RetryStrategyConfig retryTaskConfig = loadStrategyConfig(annotation.businessType(), annotation);
        taskStrategyConfigMap.put(businessType, retryTaskConfig);
    }

    public RetryStrategyConfig loadStrategyConfig(String taskType, RetryTask annotationConfig) {
        RetryStrategyConfig retryTaskConfig = null;
        if (Objects.nonNull(annotationConfig)) {
            retryTaskConfig = RetryStrategyConfig.of(annotationConfig);
        }

        RetryStrategyConfig yamlConfig = retryTaskProperties.getStrategies().get(taskType);
        if (Objects.isNull(retryTaskConfig)) {
            return yamlConfig;
        }
        retryTaskConfig.mergeFrom(yamlConfig);
        return retryTaskConfig;
    }

}
