package io.github.loadup.retrytask.notify;

/*-
 * #%L
 * Loadup Components Retrytask Notify
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging implementation of {@link RetryTaskNotifier} that logs task failures
 */
public class LoggingRetryTaskNotifier implements RetryTaskNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRetryTaskNotifier.class);

    public static final String TYPE = "log";

    @Override
    public void notify(RetryTask task) {
        logger.warn(
                ">>> [RETRY-TASK] Task failed: bizType={}, bizId={}, retryCount={}, failureReason={}",
                task.getBizType(),
                task.getBizId(),
                task.getRetryCount(),
                task.getLastFailureReason());
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
