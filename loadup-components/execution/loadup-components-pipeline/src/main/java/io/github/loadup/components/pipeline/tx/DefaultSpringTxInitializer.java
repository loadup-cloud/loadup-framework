package io.github.loadup.components.pipeline.tx;

import io.github.loadup.components.pipeline.context.PipelineContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Default {@link ITxInitializer} using Spring's PROPAGATION_REQUIRED + READ_WRITE isolation.
 *
 * <p>Register as a Spring bean and reference it in the builder:
 * <pre>{@code
 * .startTx(DefaultSpringTxInitializer.class)
 * ...
 * .endTx()
 * }</pre>
 */
public class DefaultSpringTxInitializer implements ITxInitializer {

    @Override
    public TransactionDefinition init(PipelineContext context) {
        return new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
    }
}
