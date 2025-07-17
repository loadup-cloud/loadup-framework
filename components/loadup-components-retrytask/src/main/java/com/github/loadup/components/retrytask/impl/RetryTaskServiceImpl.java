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
import com.github.loadup.components.retrytask.constant.ScheduleExecuteType;
import com.github.loadup.components.retrytask.factory.RetryStrategyFactory;
import com.github.loadup.components.retrytask.manager.RetryTaskExecuteManager;
import com.github.loadup.components.retrytask.manager.RetryTaskExecutor;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.model.RetryTaskRequest;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.transaction.RetryTaskTransactionSynchronization;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import com.github.loadup.components.tracer.TraceUtil;
import io.vavr.control.Try;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

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
    private RetryTaskRepository     retryTaskRepository;
    @Resource
    private RetryTaskExecuteManager retryTaskExecuteManager;

    /**
     * the manager of retry strategy
     */
    @Resource
    private RetryStrategyFactory taskStrategyFactory;

    /**
     * the factory of transaction templete
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RetryTaskExecutor retryTaskExecutor;

    /**
     * @see RetryTaskService#register(RetryTaskRequest)
     */
    @Override
    public RetryTaskDO register(RetryTaskRequest retryTaskRequest) {

        // check parameter
        checkParams(retryTaskRequest);
        return Try.of(() -> {
            RetryTaskDO retryTask = constructRetryTask(retryTaskRequest);
            retryTaskRepository.save(retryTask);
            processTask(retryTask, retryTaskRequest.getScheduleExecuteType());
            return retryTask;
        }).recover(throwable -> {
            RetryTaskDO existRetryTask = retryTaskRepository.findByBizId(retryTaskRequest.getBusinessId(),
                    retryTaskRequest.getBusinessType());
            if (Objects.nonNull(existRetryTask)) {
                log.warn("repeat creating retry task.bizType={},bizId={}", retryTaskRequest.getBusinessType(),
                        retryTaskRequest.getBusinessId());
                return existRetryTask;
            }
            throw new RuntimeException(throwable);
        }).get();

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
    @Transactional
    public void update(final String bizId, final String bizType) {
        // check params
        checkParams(bizId, bizType);

        final RetryStrategyConfig retryStrategyConfig = taskStrategyFactory.getStrategyConfig(bizType);

        RetryTaskDO retryTask = retryTaskRepository.lockByBizId(bizId, bizType);
        RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
        retryTaskRepository.save(retryTask);
    }

    /**
     * check parameter
     */
    private void checkParams(String bizId, String bizType) {

        if (StringUtils.isBlank(bizId) || StringUtils.isBlank(bizType)) {
            log.warn("handleTask parameter is null. bizId=", bizId, ",bizType=", bizType);
            throw new IllegalArgumentException("handleTask parameter is null");
        }
    }

    /**
     * check parameter
     */
    private void checkParams(RetryTaskRequest retryTaskRequest) {

        if (retryTaskRequest == null) {
            log.warn("parameter illegal,retryTaskRequest=null");
            throw new IllegalArgumentException("parameter illegal,retryTaskRequest=null");
        }

        checkParams(retryTaskRequest.getBusinessId(), retryTaskRequest.getBusinessType());
    }

    /**
     * construct retry task
     */
    private RetryTaskDO constructRetryTask(RetryTaskRequest retryTaskRequest) {
        RetryTaskDO retryTask = new RetryTaskDO();
        RetryStrategyConfig retryStrategyConfig =
                taskStrategyFactory.getStrategyConfig(retryTaskRequest.getBusinessType());
        retryTask.setId(IdUtils.simpleUUID()); // IdUtils.generateId(String.valueOf(retryTaskRequest.getBizId().hashCode() / 100)));
        retryTask.setBusinessId(retryTaskRequest.getBusinessId());
        retryTask.setBusinessType(retryTaskRequest.getBusinessType());
        retryTask.setRetryCount(0);
        retryTask.setNextRetryTime(
                LocalDateTime.now().plus(Duration.ofSeconds(retryTaskRequest.getStartExecuteInterval())));
        retryTask.setMaxRetries(retryStrategyConfig.getMaxRetries());
        retryTask.setProcessing(false);
        retryTask.setPayload(retryTaskRequest.getPayload());
        retryTask.setCreatedTime(LocalDateTime.now());
        retryTask.setUpdatedTime(LocalDateTime.now());
        retryTask.setPriority(retryTaskRequest.getPriority());
        retryTask.setSource(StringUtils.defaultIfBlank(retryTaskRequest.getSource(), TraceUtil.getApplicationName()));
        retryTask.setFailureCallbackUrl(retryTaskRequest.getFailureCallbackUrl());
        retryTask.setTraceId(TraceUtil.getTracerId());

        return retryTask;
    }

    /**
     * process after executing the task
     */
    private void processTask(RetryTaskDO retryTask, ScheduleExecuteType scheduleExecuteType) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new RetryTaskTransactionSynchronization(retryTask, this.retryTaskExecuteManager, scheduleExecuteType));
        } else {
            this.retryTaskExecuteManager.execute(retryTask, scheduleExecuteType);
        }
    }
}
