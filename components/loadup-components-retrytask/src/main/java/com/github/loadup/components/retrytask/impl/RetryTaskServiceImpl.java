package com.github.loadup.components.retrytask.impl;

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

import com.github.loadup.commons.util.IdUtils;
import com.github.loadup.components.retrytask.RetryTaskService;
import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.model.RetryTaskRequest;
import com.github.loadup.components.retrytask.registry.TaskStrategyRegistry;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.schedule.RetryTaskExecuter;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * The implement of Core Service of retry component
 */
@Slf4j
@Component("retryTaskService")
public class RetryTaskServiceImpl implements RetryTaskService {

    /**
     * the repository service of retry task
     */
    @Resource
    private RetryTaskRepository retryTaskRepository;

    /**
     * the manager of retry strategy
     */
    @Autowired
    private TaskStrategyRegistry retryTaskFactory;

    /**
     * the factory of transaction templete
     */
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Resource
    private RetryTaskExecuter retryTaskExecuter;

    /**
     * @see RetryTaskService#register(RetryTaskRequest)
     */
    @Override
    public RetryTask register(RetryTaskRequest retryTaskRequest) {

        // check parameter
        checkParams(retryTaskRequest);

        // construct the retry task
        RetryTask retryTask = constructRetryTask(retryTaskRequest);

        // store the retry task
        retryTaskRepository.save(retryTask);

        // process the task
        processTask(retryTask);

        return retryTask;
    }

    /**
     * @see RetryTaskService#delete(java.lang.String, java.lang.String)
     */
    @Override
    public void delete(String bizId, String bizType) {

        checkParams(bizId, bizType);
        retryTaskRepository.delete(bizId, bizType);
    }

    /**
     * @see RetryTaskService#update(java.lang.String, java.lang.String)
     */
    @Override
    public void update(final String bizId, final String bizType) {

        // check params
        checkParams(bizId, bizType);

        final RetryStrategyConfig retryStrategyConfig = retryTaskFactory.getStrategyConfig(bizType);

        // execute the transaction
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                RetryTask retryTask = retryTaskRepository.lockByBizId(bizId, bizType);
                RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
                retryTaskRepository.save(retryTask);
            }
        });
    }

    /**
     * check parameter
     */
    private void checkParams(String bizId, String bizType) {

        if (StringUtils.isBlank(bizId) || StringUtils.isBlank(bizType)) {
            log.warn("handleTask parameter is null. bizId=", bizId, ",bizType=", bizType);
            throw new RuntimeException("handleTask parameter is null");
        }
    }

    /**
     * check parameter
     */
    private void checkParams(RetryTaskRequest retryTaskRequest) {

        if (retryTaskRequest == null) {
            log.warn("parameter illegal,retryTaskRequest=null");
            throw new RuntimeException("parameter illegal,retryTaskRequest=null");
        }

        checkParams(retryTaskRequest.getBizId(), retryTaskRequest.getBizType());
    }

    /**
     * construct retry task
     */
    private RetryTask constructRetryTask(RetryTaskRequest retryTaskRequest) {
        RetryTask retryTask = new RetryTask();
        RetryStrategyConfig retryStrategyConfig =
                retryTaskFactory.getStrategyConfig(retryTaskRequest.getBizType());
        retryTask.setId(IdUtils.simpleUUID()); // IdUtils.generateId(String.valueOf(retryTaskRequest.getBizId().hashCode() / 100)));
        retryTask.setBusinessId(retryTaskRequest.getBizId());
        retryTask.setBusinessType(retryTaskRequest.getBizType());
        retryTask.setRetryCount(0);
        retryTask.setNextRetryTime(
                LocalDateTime.now().plus(Duration.ofSeconds(retryTaskRequest.getStartExecuteInterval())));
        retryTask.setMaxRetries(retryStrategyConfig.getMaxRetries());
        retryTask.setIsProcessing(false);
        retryTask.setPayload(retryTaskRequest.getPayload());
        retryTask.setCreatedTime(LocalDateTime.now());
        retryTask.setUpdatedTime(LocalDateTime.now());
        retryTask.setPriority(retryTaskRequest.getPriority());

        return retryTask;
    }

    /**
     * process after executing the task
     */
    private void processTask(RetryTask retryTask) {

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.getStrategyConfig(retryTask.getBusinessType());

        // is need execute immediately
        if (!retryStrategyConfig.isExecuteImmediately()) {
            return;
        }

        String businessKey = retryTask.getBusinessType() + RetryTaskConstants.INTERVAL_CHAR + retryTask.getBusinessId();

        retryTaskExecuter.execute(businessKey);
    }
}
