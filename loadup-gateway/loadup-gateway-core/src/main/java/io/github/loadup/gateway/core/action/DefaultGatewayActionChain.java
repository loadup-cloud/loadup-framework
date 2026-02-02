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
import java.util.concurrent.atomic.AtomicInteger;

/** Default implementation of GatewayActionChain. */
public class DefaultGatewayActionChain implements GatewayActionChain {

  private final List<GatewayAction> actions;
  private final AtomicInteger index = new AtomicInteger(0);

  public DefaultGatewayActionChain(List<GatewayAction> actions) {
    this.actions = actions;
  }

  @Override
  public void proceed(GatewayContext context) {
    if (index.get() < actions.size()) {
      GatewayAction action = actions.get(index.getAndIncrement());
      action.execute(context, this);
    }
  }
}
