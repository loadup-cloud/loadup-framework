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

import com.github.loadup.components.retrytask.RetryComponentService;
import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.config.RetryTaskFactory;
import com.github.loadup.components.retrytask.enums.ScheduleExecuteType;
import com.github.loadup.components.retrytask.manager.TaskStrategyExecutor;
import com.github.loadup.components.retrytask.manager.TaskStrategyExecutorFactory;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.model.RetryTaskRequest;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * The implement of Core Service of retry component
 */
@Slf4j
@Component("retryComponentService")
public class RetryComponentServiceImpl implements RetryComponentService {

    /**
     * the repository service of retry task
     */
    @Autowired
    private RetryTaskRepository retryTaskRepository;

    /**
     * the manager of retry strategy
     */
    @Autowired
    private RetryTaskFactory retryTaskFactory;

    /**
     * the factory of transaction templete
     */
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TaskStrategyExecutorFactory taskStrategyExecutorFactory;

    /**
     * @see RetryComponentService#register(RetryTaskRequest)
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
        processTask(retryTask, retryTaskRequest.getScheduleExecuteType());

        return retryTask;
    }

    /**
     * @see RetryComponentService#delete(java.lang.String, java.lang.String)
     */
    @Override
    public void delete(String bizId, String bizType) {

        checkParams(bizId, bizType);
        retryTaskRepository.delete(bizId, bizType);
    }

    /**
     * @see RetryComponentService#update(java.lang.String, java.lang.String)
     */
    @Override
    public void update(final String bizId, final String bizType) {

        // check params
        checkParams(bizId, bizType);

        final RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(bizType);

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

        checkParams(
                retryTaskRequest.getBizId(), retryTaskRequest.getBizType(), retryTaskRequest.getScheduleExecuteType());
    }

    /**
     * check parameter
     */
    private void checkParams(String bizId, String bizType, ScheduleExecuteType scheduleExecuteType) {

        if (StringUtils.isBlank(bizId) || StringUtils.isBlank(bizType) || scheduleExecuteType == null) {
            log.warn(
                    "register parameter is null. bizId=",
                    bizId,
                    ",bizType=",
                    bizType,
                    ",scheduleExecuteType=",
                    scheduleExecuteType);
            throw new RuntimeException("register parameter is null");
        }

        if (bizId.length() < 12) {
            log.warn("In the register process, the length of business id is illegal. bizId=", bizId);
            throw new RuntimeException("In the register process, the length of business id is illegal");
        }
    }

    /**
     * construct retry task
     */
    private RetryTask constructRetryTask(RetryTaskRequest retryTaskRequest) {
        Date now = new Date();
        RetryTask retryTask = new RetryTask();
        RetryStrategyConfig retryStrategyConfig =
                retryTaskFactory.buildRetryStrategyConfig(retryTaskRequest.getBizType());
        retryTask.setTaskId(""); // IdUtils.generateId(String.valueOf(retryTaskRequest.getBizId().hashCode() / 100)));
        retryTask.setBizId(retryTaskRequest.getBizId());
        retryTask.setBizType(retryTaskRequest.getBizType());
        retryTask.setExecutedTimes(0);
        retryTask.setNextExecuteTime(
                LocalDateTime.now().plus(Duration.ofSeconds(retryTaskRequest.getStartExecuteInterval())));
        retryTask.setMaxExecuteTimes(retryStrategyConfig.getMaxExecuteCount());
        retryTask.setProcessing(false);
        retryTask.setBizContext(retryTaskRequest.getBizContext());
        retryTask.setCreatedTime(LocalDateTime.now());
        retryTask.setModifiedTime(LocalDateTime.now());
        retryTask.setPriority(retryTaskRequest.getPriority());

        return retryTask;
    }

    /**
     * process after executing the task
     */
    private void processTask(RetryTask retryTask, ScheduleExecuteType scheduleExecuteType) {

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(retryTask.getBizType());

        // is need execute immediately
        if (!retryStrategyConfig.isExecuteImmediately()) {
            return;
        }

        TaskStrategyExecutor taskStrategyExecutor =
                taskStrategyExecutorFactory.findTaskStrategyExecutor(scheduleExecuteType);

        taskStrategyExecutor.execute(retryTask);
    }
}
