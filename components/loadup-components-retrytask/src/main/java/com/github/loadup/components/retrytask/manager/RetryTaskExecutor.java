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
import com.github.loadup.components.retrytask.config.RetryTaskFactory;
import com.github.loadup.components.retrytask.constant.RetryTaskLoggerConstants;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.model.RetryTaskExecuteResult;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import com.github.loadup.components.retrytask.spi.RetryTaskExecutorSpi;
import com.github.loadup.components.retrytask.util.RetryStrategyUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * the implement of retry task executor
 */
@Component
@Slf4j
public class RetryTaskExecutor implements TaskExecutor {
    /**
     * execute logger
     */
    private static final Logger EXE_DIGEST_LOGGER =
            LoggerFactory.getLogger(RetryTaskLoggerConstants.EXECUTE_DIGEST_NAME);

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
     * @see TaskExecutor#execute(RetryTask)
     */
    @Override
    public void execute(RetryTask retryTask) {

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(retryTask.getBizType());

        try {
            plainExecute(retryTask);
        } catch (Exception e) {

            log.error("RetryTaskExecutor execute system error,retryTask=", retryTask);
            // retry next time
            RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
            retryTaskRepository.save(retryTask);
        }
    }

    /**
     * @see TaskExecutor#plainExecute(RetryTask)
     */
    @Override
    public void plainExecute(RetryTask retryTask) {

        long startTime = System.currentTimeMillis();
        RetryTaskExecuteResult result = null;

        try {

            RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(retryTask.getBizType());

            RetryTaskExecutorSpi retryTaskExecutorSpi = retryStrategyConfig.getExecutor();

            // prepose handler
            beforeExecute(retryTask);

            // execute the SPI callback service
            result = retryTaskExecutorSpi.execute(retryTask);

            // process the result
            processResult(result, retryTask, retryStrategyConfig);

        } finally {

            EXE_DIGEST_LOGGER.info(constructExecuteDigest(retryTask, result, startTime));
            // clean MDC
            cleanMDC();
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
        digest.append(retryTask.getBizId())
                .append(',')
                .append(retryTask.getTaskId())
                .append(',')
                .append(retryTask.getBizType())
                .append(',')
                .append(retryTask.getExecutedTimes())
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
            retryTaskRepository.delete(retryTask.getBizId(), retryTask.getBizType());
        } else {
            // retry next time
            RetryStrategyUtil.updateRetryTaskByStrategy(retryTask, retryStrategyConfig);
            retryTaskRepository.save(retryTask);
        }
    }

    /**
     * prepose handler
     */
    private void beforeExecute(RetryTask retryTask) {
        // update the processing flag
        retryTask.setProcessing(true);
        retryTask.setModifiedTime(LocalDateTime.now());
        retryTaskRepository.save(retryTask);
    }
}
