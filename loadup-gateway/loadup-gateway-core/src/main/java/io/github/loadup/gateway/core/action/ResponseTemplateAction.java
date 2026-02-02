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

import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action for response template processing */
@Slf4j
public class ResponseTemplateAction implements GatewayAction {

  private final TemplateEngine templateEngine;

  public ResponseTemplateAction(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    // Proceed chain first to get response
    chain.proceed(context);

    // Post-process response
    if (context.getResponse() != null && context.getRoute().getResponseTemplate() != null) {
      try {
        GatewayResponse processedResponse =
            templateEngine.processResponseTemplate(
                context.getResponse(), context.getRoute().getResponseTemplate());
        context.setResponse(processedResponse);
      } catch (Exception e) {
        log.warn("Response template processing failed", e);
        throw GatewayExceptionFactory.templateExecutionError(
            context.getRoute().getResponseTemplate(), e);
      }
    }
  }

  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 2000;
  }
}
