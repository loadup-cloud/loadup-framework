package io.github.loadup.retrytask.facade.model;

import java.util.Map;

/**
 * Payload handed to a {@link io.github.loadup.retrytask.facade.RetryTaskProcessor} when a task is
 * executed.
 *
 * @param bizType the business type
 * @param bizId the business identifier
 * @param args the string payload registered with the task
 */
public record RetryTaskContext(String bizType, String bizId, Map<String, String> args) {}
