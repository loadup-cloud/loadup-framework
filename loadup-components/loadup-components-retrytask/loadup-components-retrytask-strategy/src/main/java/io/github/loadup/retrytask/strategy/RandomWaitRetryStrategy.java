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
import java.util.Random;

/**
 * Random wait retry strategy - waits a random time between min and max seconds
 */
public class RandomWaitRetryStrategy implements RetryStrategy {

    public static final String TYPE = "random";

    private static final Random RANDOM = new Random();
    private static final int DEFAULT_MIN_SECONDS = 10;
    private static final int DEFAULT_MAX_SECONDS = 300;

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        int minSeconds = DEFAULT_MIN_SECONDS;
        int maxSeconds = DEFAULT_MAX_SECONDS;

        // Random delay between min and max
        int randomSeconds = minSeconds + RANDOM.nextInt(maxSeconds - minSeconds + 1);
        return LocalDateTime.now().plusSeconds(randomSeconds);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
