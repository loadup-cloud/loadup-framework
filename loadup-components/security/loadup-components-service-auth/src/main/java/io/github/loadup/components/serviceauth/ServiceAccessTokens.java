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
package io.github.loadup.components.serviceauth;

/**
 * Application credentials only. Merchant/user delegation retains the original actor token.
 *
 * <p>The trusted tenant is resolved from the execution context; callers must bind it before
 * requesting a service token.
 */
@FunctionalInterface
public interface ServiceAccessTokens {
    String token(String service, String audience);
}
