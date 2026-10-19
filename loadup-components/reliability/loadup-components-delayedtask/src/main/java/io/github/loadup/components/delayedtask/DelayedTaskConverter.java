/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import org.mapstruct.Mapper;

@Mapper(config = LoadUpMapStructConfig.class)
public interface DelayedTaskConverter {
    DelayedTask toTask(DelayedTaskDO task);
}
