/*
 * #%L
 * LoadUp Transfer Client
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
package io.github.loadup.modules.transfer.client.spi;

import io.github.loadup.commons.json.ToStringAsJson;
import java.util.Map;

/** Stable task identity, validated options, and progress callback for business handlers. */
public record TransferContext(
        String taskId, String tenantId, String ownerId, Map<String, String> options, ProgressReporter progress) {
    public void report(long processed, long total) {
        progress.report(processed, total);
    }

    @FunctionalInterface
    public interface ProgressReporter {
        void report(long processed, long total);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
