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

import com.github.loadup.components.retrytask.constant.RetryTaskConstants;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * The strategy factory of retry commponet
 */
@Component
@Slf4j
@Getter
@Setter
public class RetryTaskFactory {
    /**
     * sql sentence in every database
     *
     * key:  DbType-SqlType
     * value: sql sentence
     */
    private Map<String, String>              sqlMap;
    private String                           tablePrefix;
    private String                           dbType;
    /**
     * default shedule thread pool
     */
    private ThreadPoolTaskExecutor           scheduleThreadPool;
    /**
     * retry stategy config.key: bizType
     */
    private Map<String, RetryStrategyConfig> retryStrategyConfigs = new HashMap<>();

    /**
     * obtain the retry strategy config by business type
     */
    public RetryStrategyConfig buildRetryStrategyConfig(String bizType) {

        // match by bizType first
        if (retryStrategyConfigs.get(bizType) != null) {
            return retryStrategyConfigs.get(bizType);
        }

        // match by 'DEFAULT' second
        if (retryStrategyConfigs.get(RetryTaskConstants.DEFAULT_BIZ_TYPE) != null) {
            return retryStrategyConfigs.get(RetryTaskConstants.DEFAULT_BIZ_TYPE);
        }

        return null;
    }

}
