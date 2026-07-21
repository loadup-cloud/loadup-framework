package io.github.loadup.components.pipeline.builder;

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
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Final builder step — terminates the pipeline definition.
 */
@SuppressWarnings("rawtypes")
public final class ResultAssembleBuilder extends AbstractPipelineBuilder {

    public ResultAssembleBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Close a previously opened transactional block after the assemble stage.
     */
    public ResultAssembleBuilder endTx() {
        stageList.add(EndTxMarker.class);
        return this;
    }

    /**
     * Build the immutable {@link PipelineSpec}.
     */
    public PipelineSpec build() {
        return new PipelineSpec(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
