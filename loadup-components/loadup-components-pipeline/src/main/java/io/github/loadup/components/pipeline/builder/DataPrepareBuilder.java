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

import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.api.IProcessStage;
import io.github.loadup.components.pipeline.api.IResultAssembleStage;
import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import io.github.loadup.components.pipeline.tx.ITxInitializer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Second builder step — data preparation, with optional skip forward.
 */
@SuppressWarnings("rawtypes")
public final class DataPrepareBuilder extends AbstractPipelineBuilder {

    public DataPrepareBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Add a Spring-bean data-prepare stage.
     */
    public ProcessBuilder prepare(Class<? extends IDataPrepareStage<?>> stage) {
        stageList.add(stage);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Add an inline data-prepare stage.
     */
    public <DATA> ProcessBuilder prepare(Function<PipelineContext, DATA> function) {
        registerDataPrepareFunction(function);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Skip prepare — add process stage directly.
     */
    public ProcessBuilder process(Class<? extends IProcessStage> stage) {
        stageList.add(stage);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Skip prepare — inline biz-process lambda.
     */
    public ProcessBuilder process(Consumer<PipelineContext> consumer) {
        registerBizProcessConsumer(consumer);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Skip prepare — inline data-process lambda.
     */
    public <DATA> ProcessBuilder process(BiConsumer<DATA, PipelineContext> biConsumer) {
        registerDataProcessBiConsumer(biConsumer);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Skip prepare + process — go directly to assemble.
     */
    public ResultAssembleBuilder assemble(Class<? extends IResultAssembleStage<?>> stage) {
        stageList.add(stage);
        return new ResultAssembleBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Skip prepare + process — inline assemble lambda.
     */
    public <RES> ResultAssembleBuilder assemble(Function<PipelineContext, RES> function) {
        registerResultAssembleFunction(function);
        return new ResultAssembleBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Open a transactional block.
     */
    public ProcessInTxBuilder startTx(Class<? extends ITxInitializer> txInitializer) {
        stageList.add(txInitializer);
        return new ProcessInTxBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Close a previously opened transactional block.
     */
    public ProcessBuilder endTx() {
        stageList.add(EndTxMarker.class);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
