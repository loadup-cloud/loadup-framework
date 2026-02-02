package io.github.loadup.gateway.core.action;

/*-
 * #%L
 * LoadUp Gateway Core
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

import io.github.loadup.gateway.facade.context.GatewayContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Action dispatcher */
@Slf4j
public class ActionDispatcher {

  private final List<GatewayAction> actions;

  public ActionDispatcher(List<GatewayAction> actions) {
    this.actions = actions;
  }

  /** Dispatch request to appropriate handler */
  public void dispatch(GatewayContext context) {
    if (actions == null || actions.isEmpty()) {
      log.warn("No actions configured for dispatcher");
      return;
    }

    // Execute Chain
    new DefaultGatewayActionChain(actions).proceed(context);
  }
}
