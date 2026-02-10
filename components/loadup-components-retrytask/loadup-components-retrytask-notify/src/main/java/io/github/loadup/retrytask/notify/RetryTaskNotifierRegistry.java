package io.github.loadup.retrytask.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for {@link RetryTaskNotifier}s.
 */
@Component
public class RetryTaskNotifierRegistry {

    private final Map<String, RetryTaskNotifier> notifiers = new ConcurrentHashMap<>();

    @Autowired
    public RetryTaskNotifierRegistry(List<RetryTaskNotifier> notifierList) {
        for (RetryTaskNotifier notifier : notifierList) {
            notifiers.put(notifier.getType(), notifier);
        }
    }

    /**
     * Gets the notifier for the given type.
     *
     * @param type The type of the notifier.
     * @return The notifier, or {@code null} if no notifier is found.
     */
    public RetryTaskNotifier getNotifier(String type) {
        return notifiers.get(type);
    }
}
