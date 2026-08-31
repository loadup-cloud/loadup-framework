/*-
 * #%L
 * Loadup Components Retrytask Facade
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

package io.github.loadup.retrytask.facade.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Registration payload of a retry task.
 *
 * @param bizType the business type, must be unique per processor
 * @param bizId the business identifier, unique together with {@code bizType}
 * @param args optional string payload handed to the processor
 * @param scheduleAt optional point in time when the task should run; {@code null} runs immediately
 * @param maxRetries optional retry count override; {@code null} falls back to the configured
 *     default for the business type
 */
public record RetryTaskRequest(
        String bizType, String bizId, Map<String, String> args, Instant scheduleAt, Integer maxRetries) {

    public RetryTaskRequest {
        Objects.requireNonNull(bizType, "bizType must not be null");
        Objects.requireNonNull(bizId, "bizId must not be null");
        args = args == null ? Map.of() : Map.copyOf(args);
    }

    /**
     * Creates an immediate fire-and-forget request.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     * @return the request
     */
    public static RetryTaskRequest of(String bizType, String bizId) {
        return new RetryTaskRequest(bizType, bizId, null, null, null);
    }

    /**
     * Creates an immediate fire-and-forget request with a payload.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     * @param args the payload
     * @return the request
     */
    public static RetryTaskRequest of(String bizType, String bizId, Map<String, String> args) {
        return new RetryTaskRequest(bizType, bizId, args, null, null);
    }

    /**
     * Creates a request that runs at the given point in time.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     * @param scheduleAt when to run
     * @return the request
     */
    public static RetryTaskRequest schedule(String bizType, String bizId, Instant scheduleAt) {
        return new RetryTaskRequest(bizType, bizId, null, scheduleAt, null);
    }
}
