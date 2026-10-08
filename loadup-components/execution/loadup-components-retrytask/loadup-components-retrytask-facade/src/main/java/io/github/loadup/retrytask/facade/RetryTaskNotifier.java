package io.github.loadup.retrytask.facade;

import io.github.loadup.retrytask.facade.model.RetryTaskFailure;

/**
 * SPI notified when a retry task fails permanently (all retries exhausted).
 *
 * <p>The JobRunr binder invokes every registered notifier from its failure filter. The default
 * logging notifier is always present; integrators can add channel-specific notifiers (e.g. the
 * gotone-backed one) without touching the retry pipeline.
 */
public interface RetryTaskNotifier {

    /**
     * Handles a permanently failed retry task.
     *
     * @param failure the failure details
     */
    void notifyFailed(RetryTaskFailure failure);
}
