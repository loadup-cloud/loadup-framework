package io.github.loadup.components.pipeline.builder;

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
