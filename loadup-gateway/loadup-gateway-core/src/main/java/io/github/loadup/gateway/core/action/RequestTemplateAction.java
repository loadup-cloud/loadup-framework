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
import io.github.loadup.gateway.facade.model.GatewayRequest;
import lombok.extern.slf4j.Slf4j;

/** Action for request template processing */
@Slf4j
public class RequestTemplateAction implements GatewayAction {

    private final TemplateEngine templateEngine;

    public RequestTemplateAction(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        if (context.getRoute().getRequestTemplate() != null) {
            try {
                GatewayRequest processedRequest = templateEngine.processRequestTemplate(
                        context.getRequest(), context.getRoute().getRequestTemplate());
                context.setRequest(processedRequest);
            } catch (Exception e) {
                log.warn("Request template processing failed", e);
                throw GatewayExceptionFactory.templateExecutionError(
                        context.getRoute().getRequestTemplate(), e);
            }
        }
        chain.proceed(context);
    }
}
