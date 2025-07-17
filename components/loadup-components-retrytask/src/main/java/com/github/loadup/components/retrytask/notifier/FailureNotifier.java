package com.github.loadup.components.retrytask.notifier;

import com.github.loadup.components.retrytask.model.RetryTaskDO;

public interface FailureNotifier {
    void notify(RetryTaskDO task);
}
