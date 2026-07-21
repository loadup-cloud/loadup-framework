package io.github.loadup.components.pipeline.tx;

/*-
 * #%L
 * Loadup Components Pipeline
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
