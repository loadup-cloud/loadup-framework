package io.github.loadup.retrytask.facade;

/**
 * Resolves a {@link RetryTaskProcessor} for a business type.
 */
public interface RetryTaskProcessorRegistry {

    /**
     * Returns the processor registered for the given business type.
     *
     * @param bizType the business type
     * @return the processor
     * @throws IllegalArgumentException when no processor is registered for the business type
     */
    RetryTaskProcessor getProcessor(String bizType);
}
