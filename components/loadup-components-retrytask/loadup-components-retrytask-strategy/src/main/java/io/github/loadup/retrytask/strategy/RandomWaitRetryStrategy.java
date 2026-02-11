package io.github.loadup.retrytask.strategy;

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

