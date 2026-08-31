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

import io.github.loadup.components.pipeline.api.IStage;

/**
 * Marker interface for inline (lambda-based) pipeline stages.
 *
 * <p>These are never registered as Spring beans — they are constructed directly by the
 * {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} from the lambda
 * captured in {@link io.github.loadup.components.pipeline.spec.PipelineSpec}.
 */
public interface IFunctionStage extends IStage {}
