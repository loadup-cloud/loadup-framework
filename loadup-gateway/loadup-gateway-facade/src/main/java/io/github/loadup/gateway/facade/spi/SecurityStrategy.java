package io.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
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

/**
 * Security Strategy SPI
 * Defines security logic including Authentication, Signing, and Verification.
 */
public interface SecurityStrategy {

    /**
     * Get strategy code
     * @return unique code for this strategy (e.g. "default", "OFF", "hmac-sha256")
     */
    String getCode();

    /**
     * Process security check
     * @param context Gateway Context containing request/response
     * @throws RuntimeException specific security exceptions if check fails
     */
    void process(GatewayContext context);
}
