package io.github.loadup.retrytask.example.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.strategy.RetryStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomRetryStrategy implements RetryStrategy {

    public static final String TYPE = "random";

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        long delay = ThreadLocalRandom.current().nextLong(1, 10);
        return LocalDateTime.now().plusSeconds(delay);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
