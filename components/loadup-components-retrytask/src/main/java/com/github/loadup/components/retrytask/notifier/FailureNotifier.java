package com.github.loadup.components.retrytask.notifier;

import com.github.loadup.components.retrytask.model.RetryTask;

public interface FailureNotifier {
    void notify(RetryTask task);
}
