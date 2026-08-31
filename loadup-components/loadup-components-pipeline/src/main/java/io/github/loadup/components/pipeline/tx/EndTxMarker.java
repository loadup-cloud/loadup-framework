package io.github.loadup.components.pipeline.tx;

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
 * Sentinel stage class that marks the end of a transactional block opened by
 * {@link ITxInitializer}.
 *
 * <p>Do not register this as a Spring bean — the executor constructs it internally.
 */
public final class EndTxMarker implements IStage {

    private EndTxMarker() {}
}
