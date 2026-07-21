package io.github.loadup.components.pipeline.spec;

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
import io.github.loadup.components.pipeline.stage.function.InnerBizProcessConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataPrepareFunctionStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataProcessBiConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerParamVerifyConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerResultAssembleFunctionStage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Immutable description of a pipeline: an ordered list of stage classes plus the
 * captured lambdas for any inline (function-style) stages.
 *
 * <p>Instances are created by the fluent {@link io.github.loadup.components.pipeline.builder.PipelineBuilder}
 * and are consumed by {@link io.github.loadup.components.pipeline.engine.PipelineExecutor}.
 */
@SuppressWarnings("rawtypes")
public final class PipelineSpec {

    /**
     * Ordered list of stage classes (Spring beans or inner lambda-stage markers).
     */
    private final List<Class<? extends IStage>> stageList;

    /**
     * Index → Consumer mapping for stages that are {@link InnerParamVerifyConsumerStage}
     * or {@link InnerBizProcessConsumerStage}.
     */
    private final Map<Integer, Consumer> consumerIndexMap;

    /**
     * Index → Function mapping for stages that are {@link InnerDataPrepareFunctionStage}
     * or {@link InnerResultAssembleFunctionStage}.
     */
    private final Map<Integer, Function> functionIndexMap;

    /**
     * Index → BiConsumer mapping for stages that are {@link InnerDataProcessBiConsumerStage}.
     */
    private final Map<Integer, BiConsumer> biConsumerIndexMap;

    public PipelineSpec(
            List<Class<? extends IStage>> stageList,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        this.stageList = new ArrayList<>(stageList);
        this.consumerIndexMap = new HashMap<>(consumerIndexMap);
        this.functionIndexMap = new HashMap<>(functionIndexMap);
        this.biConsumerIndexMap = new HashMap<>(biConsumerIndexMap);
    }

    public List<Class<? extends IStage>> getStageList() {
        return Collections.unmodifiableList(stageList);
    }

    public Map<Integer, Consumer> getConsumerIndexMap() {
        return Collections.unmodifiableMap(consumerIndexMap);
    }

    public Map<Integer, Function> getFunctionIndexMap() {
        return Collections.unmodifiableMap(functionIndexMap);
    }

    public Map<Integer, BiConsumer> getBiConsumerIndexMap() {
        return Collections.unmodifiableMap(biConsumerIndexMap);
    }
}
