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
