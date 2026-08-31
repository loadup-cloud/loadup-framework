package io.github.loadup.components.pipeline.tx;

/*-
 * #%L
 * Loadup Components Pipeline
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
