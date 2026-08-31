package io.github.loadup.components.pipeline.api;

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

/**
 * Parameter-validation stage SPI.
 *
 * <p>The stage receives the raw request object and should throw a {@link RuntimeException}
 * (e.g. {@code IllegalArgumentException}) when validation fails, which will then be routed
 * to the configured {@link io.github.loadup.components.pipeline.exception.IExceptionClassHandler}.
 *
 * @param <REQ> request type
 */
public interface IParamVerifyStage<REQ> extends IStage {

    /**
     * Validate the incoming request.
     *
     * @param req the raw request; never {@code null} when called by the pipeline executor
     */
    void verify(REQ req);
}
