package com.github.loadup.components.retrytask.registry;

import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.notifier.FailureNotifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FailureNotifierRegistry {

    private final List<FailureNotifier> notifiers = new ArrayList<>();

    public void register(FailureNotifier notifier) {
        notifiers.add(notifier);
    }

    public void notifyAll(RetryTaskDO task) {
        for (FailureNotifier notifier : notifiers) {
            notifier.notify(task);
        }
    }
}
