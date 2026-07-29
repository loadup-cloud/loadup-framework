package io.github.loadup.components.gotone.record;

/*-
 * #%L
 * Loadup Gotone API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;

public interface RecordHandler {
    void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result);

    record SendAttemptResult(
            String actualProvider,
            boolean success,
            int successCount,
            int failedCount,
            java.util.List<Attempt> attempts) {}

    record Attempt(String providerName, boolean success, String error) {}
}
