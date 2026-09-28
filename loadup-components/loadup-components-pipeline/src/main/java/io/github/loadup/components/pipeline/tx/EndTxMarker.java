package io.github.loadup.components.pipeline.tx;

import io.github.loadup.components.pipeline.api.IStage;

/**
 * Sentinel stage class that marks the end of a transactional block opened by
 * {@link ITxInitializer}.
 *
 * <p>Do not register this as a Spring bean — the executor constructs it internally.
 */
public final class EndTxMarker implements IStage {

    private EndTxMarker() {}
}
