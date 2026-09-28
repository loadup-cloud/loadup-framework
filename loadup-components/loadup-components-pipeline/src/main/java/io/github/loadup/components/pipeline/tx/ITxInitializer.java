package io.github.loadup.components.pipeline.tx;

import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import org.springframework.transaction.TransactionDefinition;

/**
 * Transaction-initialiser SPI.
 *
 * <p>Add an implementation class to the pipeline via
 * {@link io.github.loadup.components.pipeline.builder.ProcessBuilder#startTx(Class)}.
 * The executor will wrap all subsequent stages (until the matching {@link EndTxMarker})
 * in a {@link org.springframework.transaction.support.TransactionTemplate} configured
 * by the {@link TransactionDefinition} returned by {@link #init}.
 *
 * <p>A default implementation using PROPAGATION_REQUIRED is available via
 * {@link DefaultSpringTxInitializer}.
 */
public interface ITxInitializer extends IStage {

    /**
     * Return the Spring {@link TransactionDefinition} to apply to the TX block.
     *
     * @param context the current pipeline context
     * @return transaction definition; never {@code null}
     */
    TransactionDefinition init(PipelineContext context);
}
