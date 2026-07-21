package io.github.loadup.components.pipeline.stage.function;

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

import io.github.loadup.components.pipeline.api.IParamVerifyStage;
import java.util.function.Consumer;

/**
 * Lambda adapter for {@link IParamVerifyStage}.
 */
public final class InnerParamVerifyConsumerStage<REQ> implements IFunctionStage, IParamVerifyStage<REQ> {

    private final Consumer<REQ> consumer;

    public InnerParamVerifyConsumerStage(Consumer<REQ> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void verify(REQ req) {
        consumer.accept(req);
    }
}
