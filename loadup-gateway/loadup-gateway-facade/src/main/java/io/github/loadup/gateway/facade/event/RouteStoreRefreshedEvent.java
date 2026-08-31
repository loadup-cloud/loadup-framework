/*-
 * #%L
 * LoadUp Gateway Facade
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
package io.github.loadup.gateway.facade.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published whenever a {@link io.github.loadup.gateway.facade.spi.RouteStore} reloads its
 * route definitions. The gateway engine listens for this event and recompiles its routing
 * table. Any store implementation (YAML file watch, database poll, config center push)
 * should publish this event after a successful reload.
 */
public class RouteStoreRefreshedEvent extends ApplicationEvent {

    public RouteStoreRefreshedEvent(Object source) {
        super(source);
    }
}
