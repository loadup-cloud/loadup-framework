/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.asyncresult;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import tools.jackson.databind.json.JsonMapper;

final class AsyncResultIntegrationFixtures {
    private AsyncResultIntegrationFixtures() {}

    static AsyncResultProperties settings(int pending, Duration timeout) {
        return new AsyncResultProperties(
                true,
                "local",
                "test",
                timeout,
                Duration.ofSeconds(5),
                pending,
                Duration.ofMinutes(1),
                Duration.ofMillis(10),
                1024,
                100);
    }

    static AsyncResultOptions options() {
        return new AsyncResultOptions("echo.v1", "order", null);
    }

    static CompletableFuture<Void> sent() {
        return CompletableFuture.completedFuture(null);
    }

    static DefaultAsyncResultTemplate template(AsyncResultProvider provider, AsyncResultProperties settings) {
        return new DefaultAsyncResultTemplate(
                provider, settings, JsonMapper.builder().build(), null);
    }

    static AsyncReplyKey key() {
        return new AsyncReplyKey("echo", "order", UUID.randomUUID().toString());
    }
}
