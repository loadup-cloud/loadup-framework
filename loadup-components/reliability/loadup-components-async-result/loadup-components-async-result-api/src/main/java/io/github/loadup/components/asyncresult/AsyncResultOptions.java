/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult;

import io.github.loadup.commons.json.ToStringAsJson;
import java.time.Duration;

public record AsyncResultOptions(String tenantId, String operation, String businessId, Duration waitTimeout) {
    public AsyncResultOptions {
        AsyncReplyKey.required(tenantId, 64);
        AsyncReplyKey.required(operation, 128);
        AsyncReplyKey.required(businessId, 128);
        if (waitTimeout != null && (waitTimeout.isNegative() || waitTimeout.isZero()))
            throw new IllegalArgumentException("Positive waitTimeout is required");
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
