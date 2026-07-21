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
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import io.github.loadup.components.pipeline.tx.ITxInitializer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Third builder step — chained process stages, with optional assemble / tx controls.
 */
@SuppressWarnings("rawtypes")
public final class ProcessBuilder extends AbstractPipelineBuilder {

    public ProcessBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Chain another Spring-bean process stage.
     */
    public ProcessBuilder process(Class<? extends IProcessStage> stage) {
        stageList.add(stage);
        return this;
    }

    /**
     * Chain an inline biz-process lambda.
     */
    public ProcessBuilder process(Consumer<PipelineContext> consumer) {
        registerBizProcessConsumer(consumer);
        return this;
    }

    /**
     * Chain an inline data-process lambda.
     */
    public <DATA> ProcessBuilder process(BiConsumer<DATA, PipelineContext> biConsumer) {
        registerDataProcessBiConsumer(biConsumer);
        return this;
    }

    /**
     * Chain a Spring-bean data-prepare stage (re-prepare mid-pipeline).
     */
    public ProcessBuilder prepare(Class<? extends IDataPrepareStage<?>> stage) {
        stageList.add(stage);
        return this;
    }

    /**
     * Chain an inline data-prepare lambda.
     */
    public <DATA> ProcessBuilder prepare(Function<PipelineContext, DATA> function) {
        registerDataPrepareFunction(function);
        return this;
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
        return this;
    }

    /**
     * Add a Spring-bean assemble stage and move to the final builder step.
     */
    public ResultAssembleBuilder assemble(Class<? extends IResultAssembleStage<?>> stage) {
        stageList.add(stage);
        return new ResultAssembleBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Add an inline assemble lambda and move to the final builder step.
     */
    public <RES> ResultAssembleBuilder assemble(Function<PipelineContext, RES> function) {
        registerResultAssembleFunction(function);
        return new ResultAssembleBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Terminate the pipeline without an explicit assemble stage.
     */
    public PipelineSpec build() {
        return new PipelineSpec(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
