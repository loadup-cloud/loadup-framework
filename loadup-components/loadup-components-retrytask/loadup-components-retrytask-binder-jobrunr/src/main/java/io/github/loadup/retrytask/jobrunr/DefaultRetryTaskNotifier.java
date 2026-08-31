/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link RetryTaskNotifier} that logs permanent failures.
 *
 * <p>Integrators that need richer alerts (email, SMS, webhook) can add another notifier bean, e.g.
 * the gotone-backed {@code GotoneRetryTaskNotifier}; every registered notifier is invoked.
 */
public class DefaultRetryTaskNotifier implements RetryTaskNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRetryTaskNotifier.class);

    @Override
    public void notifyFailed(RetryTaskFailure failure) {
        LOGGER.warn(
                "Retry task permanently failed: bizType={} bizId={} jobId={} attempts={} reason={}",
                failure.bizType(),
                failure.bizId(),
                failure.jobId(),
                failure.attempts(),
                failure.errorMessage());
    }
}
