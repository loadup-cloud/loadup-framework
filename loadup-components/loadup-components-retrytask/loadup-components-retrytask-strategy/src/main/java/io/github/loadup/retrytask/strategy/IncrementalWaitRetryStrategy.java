package io.github.loadup.retrytask.strategy;

/*-
 * #%L
 * Loadup Components Retrytask Strategy
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
import java.time.LocalDateTime;

/**
 * Incremental wait retry strategy - wait time increases linearly
 * Formula: delay = baseDelay * (retryCount + 1)
 */
public class IncrementalWaitRetryStrategy implements RetryStrategy {

    public static final String TYPE = "incremental";

    private static final int DEFAULT_BASE_DELAY_SECONDS = 30;

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        int baseDelay = DEFAULT_BASE_DELAY_SECONDS;

        // Linear increase: 30s, 60s, 90s, 120s, ...
        long delaySeconds = baseDelay * (task.getRetryCount() + 1);

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
