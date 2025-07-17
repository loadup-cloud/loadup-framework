package com.github.loadup.components.retrytask.transaction;

import com.github.loadup.components.retrytask.constant.ScheduleExecuteType;
import com.github.loadup.components.retrytask.manager.RetryTaskExecuteManager;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import org.springframework.transaction.support.TransactionSynchronization;

public class RetryTaskTransactionSynchronization implements TransactionSynchronization {
    private RetryTaskDO             retryTask;
    private RetryTaskExecuteManager retryTaskExecuteManager;
    private ScheduleExecuteType     scheduleExecuteType;

    public RetryTaskTransactionSynchronization(RetryTaskDO retryTask, RetryTaskExecuteManager retryTaskExecuteManager,
                                               ScheduleExecuteType scheduleExecuteType) {
        this.retryTask = retryTask;
        this.retryTaskExecuteManager = retryTaskExecuteManager;
        this.scheduleExecuteType = scheduleExecuteType;
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            this.retryTaskExecuteManager.execute(this.retryTask, this.scheduleExecuteType);
        }
    }
}
