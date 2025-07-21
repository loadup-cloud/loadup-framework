package com.github.loadup.components.retrytask.manager;

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
import com.github.loadup.components.retrytask.constant.RetryTaskLoggerConstants;
import com.github.loadup.components.retrytask.factory.FailureNotifierFactory;
import com.github.loadup.components.retrytask.factory.RetryStrategyFactory;
import com.github.loadup.components.retrytask.handler.RetryTaskExecutorHandler;
import com.github.loadup.components.retrytask.log.RetryTaskDigestLogger;
import com.github.loadup.components.retrytask.model.*;
import com.github.loadup.components.retrytask.repository.RetryTaskHistoryRepository;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import com.github.loadup.components.tracer.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * retry task executer
 */
@Component
@Slf4j(topic = RetryTaskLoggerConstants.EXECUTE_DIGEST_NAME)
public class RetryTaskExecutor {

    /**
     * the repository of retry task
     */
    @Autowired
    private RetryTaskRepository retryTaskRepository;

    @Autowired
    private RetryTaskHistoryRepository retryTaskHistoryRepository;
    /**
     * the manager of retry strategy
     */
    @Autowired
    private RetryStrategyFactory retryStrategyFactory;



    @Autowired
    private FailureNotifierFactory failureNotifierFactory;
    @Autowired
    private RetryTaskDigestLogger  digestLogger;

    /**
     * execute
     */
    public void plainExecute(RetryTaskDO retryTask) {
        long startTime = System.currentTimeMillis();
        RetryTaskExecuteResult result = null;

        try {
            RetryStrategyConfig retryStrategyConfig = this.retryStrategyFactory.getStrategyConfig(retryTask.getBusinessType());
            RetryTaskExecutorHandler<?> taskHandler = retryStrategyFactory.getTaskHandler(retryTask.getBusinessType());
            this.beforeExecute(retryTask);
            result = taskHandler.execute(retryTask);
            this.processResult(result, retryTask, retryStrategyConfig);
        } finally {
            log.info(constructExecuteDigest(retryTask, result, startTime));
        }
    }

    private void processResult(RetryTaskExecuteResult result, RetryTaskDO retryTask, RetryStrategyConfig retryStrategyConfig) {
        if (!result.isProcessing()) {
            if (result.isSuccess()) {
                asyncArchiveTask(retryTask);
            } else {
                RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
                retryTaskRepository.save(retryTask);
            }
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void asyncArchiveTask(RetryTaskDO task) {
        RetryTaskHistoryDO history = new RetryTaskHistoryDO();
        BeanUtils.copyProperties(task, history);
        history.setFinishedTime(LocalDateTime.now());
        if (!retryTaskHistoryRepository.existsById(task.getId())) {
            retryTaskHistoryRepository.save(history);
        }
        if (retryTaskRepository.existsById(task.getId())) {
            retryTaskRepository.deleteById(task.getId());
        }
    }

    private void beforeExecute(RetryTaskDO retryTask) {

        if (retryTask.getTraceId() != null) {
            TraceUtil.logTraceId(retryTask.getTraceId());
        }
        retryTask.setProcessing(true);
        retryTask.setUpdatedTime(LocalDateTime.now());
        this.retryTaskRepository.save(retryTask);
    }

    /**
     * process
     */
    private RetryTaskDO process(String businessKey) {
        return null;
    }

    /**
     * postProcess if retryTaskNeedUpdate is not null
     */
    private void doAfterProcess(RetryTaskDO retryTaskNeedUpdate, String businessKey) {
        // if process fail, then
        if (Objects.nonNull(retryTaskNeedUpdate)) {
            try {
                // retry next time
                RetryStrategyConfig retryStrategyConfig = retryStrategyFactory.getStrategyConfig(retryTaskNeedUpdate.getBusinessType());
                RetryStrategyUtil.updateRetryTaskByStrategy(retryTaskNeedUpdate, retryStrategyConfig);
                retryTaskRepository.save(retryTaskNeedUpdate);
            } catch (Exception e) {
                //                LogUtils.error(log, "the task update failed when do after process, businessKey={}",
                // businessKey);
            }
        }
    }

    public void execute(RetryTaskDO retryTask) {
        RetryStrategyConfig retryStrategyConfig = this.retryStrategyFactory.getStrategyConfig(retryTask.getBusinessType());

        try {
            plainExecute(retryTask);
        } catch (Exception e) {
            log.error("RetryTaskExecutor execute system error,retryTask={}", retryTask);
            //retryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
            //retryTaskRepository.update(retryTask);
        }
    }

    private String constructExecuteDigest(RetryTaskDO retryTask, RetryTaskExecuteResult<?> executeResult, long startTime) {

        StringBuffer digest = new StringBuffer();
        long elapseTime = System.currentTimeMillis() - startTime;
        digest.append(retryTask.getBusinessId()).append(',').append(retryTask.getId()).append(',').append(retryTask.getBusinessType())
                .append(',').append(retryTask.getRetryCount()).append(',').append(executeResult != null && executeResult.isSuccess())
                .append(',').append(elapseTime).append("ms");
        return digest.toString();
    }
}
