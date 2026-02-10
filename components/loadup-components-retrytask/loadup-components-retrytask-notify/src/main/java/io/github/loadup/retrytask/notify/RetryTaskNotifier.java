package io.github.loadup.retrytask.notify;

import io.github.loadup.retrytask.facade.model.RetryTask;

/**
 * A notifier for retry tasks.
 */
public interface RetryTaskNotifier {

    /**
     * Notifies that a task has failed.
     *
     * @param task The task that has failed.
     */
    void notify(RetryTask task);

    /**
     * Gets the type of the notifier.
     *
     * @return The type of the notifier.
     */
    String getType();
}
