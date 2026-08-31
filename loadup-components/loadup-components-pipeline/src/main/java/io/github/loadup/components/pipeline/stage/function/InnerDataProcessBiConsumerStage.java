package io.github.loadup.components.pipeline.stage.function;

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

import io.github.loadup.components.pipeline.api.IDataProcessStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.BiConsumer;

/**
 * Lambda adapter for {@link IDataProcessStage}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class InnerDataProcessBiConsumerStage implements IFunctionStage, IDataProcessStage {

    private final BiConsumer<Object, PipelineContext> biConsumer;

    public InnerDataProcessBiConsumerStage(BiConsumer<?, PipelineContext> biConsumer) {
        this.biConsumer = (BiConsumer<Object, PipelineContext>) biConsumer;
    }

    @Override
    public void process(Object data, PipelineContext context) {
        biConsumer.accept(data, context);
    }
}
