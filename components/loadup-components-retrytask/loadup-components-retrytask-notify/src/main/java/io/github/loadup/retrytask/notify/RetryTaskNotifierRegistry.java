package io.github.loadup.retrytask.notify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link RetryTaskNotifier}s
 */
public class RetryTaskNotifierRegistry {

    private final Map<String, RetryTaskNotifier> notifiers = new ConcurrentHashMap<>();

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

    public void register(RetryTaskNotifier notifier) {
        notifiers.put(notifier.getType(), notifier);
    }

}
