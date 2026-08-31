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

import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.Function;

/**
 * Lambda adapter for {@link IDataPrepareStage}.
 */
@SuppressWarnings("rawtypes")
public final class InnerDataPrepareFunctionStage implements IFunctionStage, IDataPrepareStage {

    private final Function<PipelineContext, Object> function;

    @SuppressWarnings("unchecked")
    public InnerDataPrepareFunctionStage(Function<PipelineContext, ?> function) {
        this.function = (Function<PipelineContext, Object>) function;
    }

    @Override
    public Object prepare(PipelineContext context) {
        return function.apply(context);
    }
}
