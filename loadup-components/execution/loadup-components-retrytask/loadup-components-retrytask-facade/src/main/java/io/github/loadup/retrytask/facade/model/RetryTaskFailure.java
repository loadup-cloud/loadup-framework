package io.github.loadup.retrytask.facade.model;

import java.util.UUID;

/**
 * Details of a permanently failed retry task.
 *
 * @param bizType the business type
 * @param bizId the business identifier
 * @param jobId the underlying job id
 * @param attempts the number of attempts made
 * @param errorMessage the final error message
 */
public record RetryTaskFailure(String bizType, String bizId, UUID jobId, int attempts, String errorMessage) {}
