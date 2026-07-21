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
 * Exponential backoff retry strategy - wait time increases exponentially
 * Formula: delay = 2^retryCount minutes
 * Example: 2min, 4min, 8min, 16min, 32min, ...
 */
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    public static final String TYPE = "exponential";

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        long delay = (long) Math.pow(2, task.getRetryCount());
        return LocalDateTime.now().plusMinutes(delay);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
