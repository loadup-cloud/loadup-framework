package com.github.loadup.components.retrytask.transaction;

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

import com.github.loadup.components.retrytask.enums.ScheduleExecuteType;
import com.github.loadup.components.retrytask.manager.TaskStrategyExecutor;
import com.github.loadup.components.retrytask.manager.TaskStrategyExecutorFactory;
import com.github.loadup.components.retrytask.model.RetryTask;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * 重试任务事务同步回调方法
 */
public class RetryTaskTransactionSynchronization implements TransactionSynchronization {

    /**
     * 重试任务
     */
    private RetryTask retryTask;

    /**
     * 重试任务执行管理器
     */
    private TaskStrategyExecutorFactory taskStrategyExecutorFactory;

    /**
     * 执行策略类型
     */
    private ScheduleExecuteType scheduleExecuteType;

    /**
     * 构造函数
     */
    public RetryTaskTransactionSynchronization(
            RetryTask retryTask,
            TaskStrategyExecutorFactory taskStrategyExecutorFactory,
            ScheduleExecuteType scheduleExecuteType) {
        this.retryTask = retryTask;
        this.taskStrategyExecutorFactory = taskStrategyExecutorFactory;
        this.scheduleExecuteType = scheduleExecuteType;
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#suspend()
     */
    @Override
    public void suspend() {
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#resume()
     */
    @Override
    public void resume() {
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
     */
    @Override
    public void beforeCommit(boolean readOnly) {
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion()
     */
    @Override
    public void beforeCompletion() {
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#afterCommit()
     */
    @Override
    public void afterCommit() {
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)
     */
    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_COMMITTED) {
            TaskStrategyExecutor taskStrategyExecutor =
                    taskStrategyExecutorFactory.findTaskStrategyExecutor(scheduleExecuteType);
            taskStrategyExecutor.execute(retryTask);
        }
    }

    /**
     * @see org.springframework.transaction.support.TransactionSynchronization#flush()
     */
    @Override
    public void flush() {
    }
}
