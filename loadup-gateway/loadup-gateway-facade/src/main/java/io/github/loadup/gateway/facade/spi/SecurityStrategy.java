package io.github.loadup.gateway.facade.spi;

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

import io.github.loadup.gateway.facade.context.GatewayContext;

/**
 * Security Strategy SPI
 * Defines security logic including Authentication, Signing, and Verification.
 */
public interface SecurityStrategy {

    /**
     * Get strategy code
     *
     * @return unique code for this strategy (e.g. "default", "OFF", "hmac-sha256")
     */
    String getCode();

    /**
     * Process security check
     *
     * @param context Gateway Context containing request/response
     * @throws RuntimeException specific security exceptions if check fails
     */
    void process(GatewayContext context);
}
