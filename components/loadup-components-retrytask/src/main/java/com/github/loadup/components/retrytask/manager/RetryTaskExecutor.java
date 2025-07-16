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
import com.github.loadup.components.retrytask.handler.RetryTaskExecutorHandler;
import com.github.loadup.components.retrytask.model.*;
import com.github.loadup.components.retrytask.registry.TaskHandlerRegistry;
import com.github.loadup.components.retrytask.registry.TaskStrategyRegistry;
import com.github.loadup.components.retrytask.repository.RetryTaskHistoryRepository;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * the implement of retry task executor
 */
@Component
@Slf4j(topic = RetryTaskLoggerConstants.EXECUTE_DIGEST_NAME)
public class RetryTaskExecutor {

    @Autowired
    private RetryTaskRepository        retryTaskRepository;
    @Autowired
    private RetryTaskHistoryRepository retryTaskHistoryRepository;
    @Autowired
    private TaskStrategyRegistry       retryTaskFactory;
    @Autowired
    private TaskHandlerRegistry        taskHandlerRegistry;

    /**
     * @see TaskExecutor#execute(RetryTask)
     */

    public void execute(RetryTask retryTask) {

        long startTime = System.currentTimeMillis();
        RetryTaskExecuteResult result = null;

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.getStrategyConfig(retryTask.getBusinessType());
        try {

            RetryTaskExecutorHandler retryTaskExecutorHandler = taskHandlerRegistry.get(retryTask.getBusinessType());
            if (retryTaskExecutorHandler == null) {
                throw new IllegalArgumentException("No handler found for taskType: " + retryTask.getBusinessType());
            }
            // prepose handler
            beforeExecute(retryTask);

            // execute the SPI callback service
            result = retryTaskExecutorHandler.execute(retryTask);

            // process the result
            processResult(result, retryTask, retryStrategyConfig);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("RetryTaskExecutor execute system error,retryTask=", retryTask);
        } finally {
            log.info(constructExecuteDigest(retryTask, result, startTime));
            cleanMDC();
            RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
            retryTaskRepository.save(retryTask);
        }
    }

    private void cleanMDC() {
        // MDC.remove(MDC_TRACEID);
        // MDC.remove(MDC_SOFA_TRACEID);
        // MDC.remove(MDC_ISLOADTEST);
    }

    /**
     * constuct the digest of execute
     */
    private String constructExecuteDigest(RetryTask retryTask, RetryTaskExecuteResult executeResult, long startTime) {

        StringBuffer digest = new StringBuffer();
        long elapseTime = System.currentTimeMillis() - startTime;
        digest.append(retryTask.getBusinessId())
                .append(',')
                .append(retryTask.getId())
                .append(',')
                .append(retryTask.getBusinessType())
                .append(',')
                .append(retryTask.getRetryCount())
                .append(',')
                .append(executeResult != null && executeResult.isSuccess())
                .append(',')
                .append(elapseTime)
                .append("ms");
        return digest.toString();
    }

    /**
     * prepose handler
     */
    private void processResult(
            RetryTaskExecuteResult result, RetryTask retryTask, RetryStrategyConfig retryStrategyConfig) {

        // the task is processing asynchronized, we need do nothing
        if (result.isProcessing()) {
            return;
        }
        if (result.isSuccess()) {
            // delete the task
            //retryTaskRepository.delete(retryTask.getBusinessId(), retryTask.getBusinessType());

            // 成功后直接归档
            asyncArchiveTask(retryTask);
        } else {
            // retry next time
            RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
            retryTaskRepository.save(retryTask);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void asyncArchiveTask(RetryTask task) {
        RetryTaskHistory history = new RetryTaskHistory();
        BeanUtils.copyProperties(task,history);
        history.setFinishedTime(LocalDateTime.now());
        retryTaskHistoryRepository.save(history);
        retryTaskRepository.deleteById(task.getId());
    }

    /**
     * prepose handler
     */
    private void beforeExecute(RetryTask retryTask) {
        // update the processing flag
        retryTask.setIsProcessing(true);
        retryTask.setUpdatedTime(LocalDateTime.now());
        retryTaskRepository.save(retryTask);
    }
}
