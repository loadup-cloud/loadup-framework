package com.github.loadup.components.retrytask.schedule;

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
import com.github.loadup.components.retrytask.config.RetryTaskFactory;
import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
import com.github.loadup.components.retrytask.manager.RetryTaskExecutor;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * retry task executer
 */
@Component
@Slf4j
public class RetryTaskExecuter {

    /**
     * the repository of retry task
     */
    @Autowired
    private RetryTaskRepository retryTaskRepository;

    /**
     * the manager of retry strategy
     */
    @Autowired
    private RetryTaskFactory retryTaskFactory;

    /**
     * the executor of retry task
     */
    @Autowired
    private RetryTaskExecutor retryTaskExecutor;

    /**
     * execute
     */
    public void execute(String businessKey) {
        RetryTask retryTaskNeedUpdate = process(businessKey);
        doAfterProcess(retryTaskNeedUpdate, businessKey);
    }

    /**
     * process
     */
    private RetryTask process(String businessKey) {

        String[] subStrings = StringUtils.split(businessKey, RetryTaskConstants.INTERVAL_CHAR, 2);
        if (subStrings == null || subStrings.length != 2) {
            log.warn("businessKey is illegal, {}", businessKey);
            return null;
        }

        final String bizType = subStrings[0];
        final String bizId = subStrings[1];

        RetryTask retryTask = null;
        try {
            retryTask = retryTaskRepository.findByBizId(bizId, bizType);

            if (retryTask == null) {
                log.warn("the task is not existed ,bizId=", bizId, ",bizType=", bizType);
                return null;
            }

            retryTaskExecutor.plainExecute(retryTask);

            // if process success, then return null, out layer will do postProcess by check whether retryTask is null or
            // not
            return null;

        } catch (Exception exception) {
            log.warn("the task executed failed ,bizId=", bizId, ",bizType=", bizType);
        }

        return retryTask;
    }

    /**
     * postProcess if retryTaskNeedUpdate is not null
     */
    private void doAfterProcess(RetryTask retryTaskNeedUpdate, String businessKey) {
        // if process fail, then
        if (Objects.nonNull(retryTaskNeedUpdate)) {
            try {
                // retry next time
                RetryStrategyConfig retryStrategyConfig =
                        retryTaskFactory.buildRetryStrategyConfig(retryTaskNeedUpdate.getBizType());
                RetryStrategyUtil.updateRetryTaskByStrategy(retryTaskNeedUpdate, retryStrategyConfig);
                retryTaskRepository.save(retryTaskNeedUpdate);
            } catch (Exception e) {
                //                LogUtils.error(log, "the task update failed when do after process, businessKey={}",
                // businessKey);
            }
        }
    }
}
