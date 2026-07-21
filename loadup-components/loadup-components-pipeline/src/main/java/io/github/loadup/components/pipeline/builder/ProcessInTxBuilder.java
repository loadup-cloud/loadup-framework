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
 * Builder step used inside an open transactional block.
 * The TX ends when {@link #endTx()} is called, which returns a {@link ProcessBuilder}.
 */
@SuppressWarnings("rawtypes")
public final class ProcessInTxBuilder extends AbstractPipelineBuilder {

    public ProcessInTxBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Chain a Spring-bean process stage inside the TX block.
     */
    public ProcessInTxBuilder process(Class<? extends IProcessStage> stage) {
        stageList.add(stage);
        return this;
    }

    /**
     * Chain an inline biz-process lambda inside the TX block.
     */
    public ProcessInTxBuilder process(Consumer<PipelineContext> consumer) {
        registerBizProcessConsumer(consumer);
        return this;
    }

    /**
     * Chain an inline data-process lambda inside the TX block.
     */
    public <DATA> ProcessInTxBuilder process(BiConsumer<DATA, PipelineContext> biConsumer) {
        registerDataProcessBiConsumer(biConsumer);
        return this;
    }

    /**
     * Chain a data-prepare stage inside the TX block.
     */
    public ProcessInTxBuilder prepare(Class<? extends IDataPrepareStage<?>> stage) {
        stageList.add(stage);
        return this;
    }

    /**
     * Chain an inline data-prepare lambda inside the TX block.
     */
    public <DATA> ProcessInTxBuilder prepare(Function<PipelineContext, DATA> function) {
        registerDataPrepareFunction(function);
        return this;
    }

    /**
     * Nest another TX initialiser (e.g. for savepoint semantics).
     */
    public ProcessInTxBuilder startTx(Class<? extends ITxInitializer> txInitializer) {
        stageList.add(txInitializer);
        return this;
    }

    /**
     * Close the transactional block and return to the outer process builder.
     */
    public ProcessBuilder endTx() {
        stageList.add(EndTxMarker.class);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
