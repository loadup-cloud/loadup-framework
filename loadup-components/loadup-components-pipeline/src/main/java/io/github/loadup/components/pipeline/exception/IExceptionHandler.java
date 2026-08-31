package io.github.loadup.components.pipeline.exception;

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

import io.github.loadup.components.pipeline.context.PipelineContext;

/**
 * Top-level exception-handler SPI.
 *
 * @param <RES> the response type returned on exception
 */
public interface IExceptionHandler<RES> {

    /**
     * Side-effect hook called before building the error response (e.g. logging, alerting).
     * Default implementation is a no-op.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     */
    default void handleException(Throwable t, PipelineContext context) {}

    /**
     * Build the error response.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     * @return the error response; must not be {@code null}
     */
    RES assembleResultOnException(Throwable t, PipelineContext context);
}
