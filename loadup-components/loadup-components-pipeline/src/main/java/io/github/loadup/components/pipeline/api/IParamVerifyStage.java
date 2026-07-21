package io.github.loadup.components.pipeline.api;

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
