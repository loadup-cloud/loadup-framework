/*
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import io.github.loadup.commons.util.internal.InternalThreadLocalMap;
import java.security.SecureRandom;
import java.util.UUID;

public class IdUtils {

    private static final SecureRandom random = new SecureRandom();

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String uuid2() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static long randomLong() {
        return random.nextLong() & Long.MAX_VALUE;
    }

    /**
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom() {
        return new SecureRandom();
    }

    /**
     * @return
     */
    public static String getRequestd() {
        return InternalThreadLocalMap.get().getRequestId();
    }

    /**
     * @param requestId
     */
    public static void setRequestId(String requestId) {
        InternalThreadLocalMap.get().setRequestId(requestId);
    }
}
