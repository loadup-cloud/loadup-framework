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
