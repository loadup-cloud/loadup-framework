package io.github.loadup.components.gotone.api;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;

/**
 * Notification Service Interface.
 *
 * <p>Main entry point for sending notifications.
 */
public interface NotificationService {

    /**
     * Send notification synchronously.
     *
     * <p>Process:
     * <ol>
     *   <li>Lookup template by bizCode from database</li>
     *   <li>Render template content using templateParams</li>
     *   <li>Select appropriate binder based on channel</li>
     *   <li>Select provider through Extension mechanism</li>
     *   <li>Send notification</li>
     * </ol>
     *
     * @param request notification request
     * @return notification response
     */
    NotificationResponse send(NotificationRequest request);

    /**
     * Send notification asynchronously.
     *
     * @param request notification request
     */
    void sendAsync(NotificationRequest request);


}
