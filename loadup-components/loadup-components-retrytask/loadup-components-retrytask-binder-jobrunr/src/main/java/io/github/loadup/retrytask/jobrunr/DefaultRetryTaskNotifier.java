package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;

/**
 * Default {@link RetryTaskNotifier} that logs permanent failures.
 *
 * <p>Integrators that need richer alerts (email, SMS, webhook) can add another notifier bean, e.g.
 * the gotone-backed {@code GotoneRetryTaskNotifier}; every registered notifier is invoked.
 */
public class DefaultRetryTaskNotifier implements RetryTaskNotifier {

    @Override
    public void notifyFailed(RetryTaskFailure failure) {
        LogUtil.warn(
                DefaultRetryTaskNotifier.class,
                "Retry task permanently failed: bizType={} bizId={} jobId={} attempts={} reason={}",
                failure.bizType(),
                failure.bizId(),
                failure.jobId(),
                failure.attempts(),
                failure.errorMessage());
    }
}
