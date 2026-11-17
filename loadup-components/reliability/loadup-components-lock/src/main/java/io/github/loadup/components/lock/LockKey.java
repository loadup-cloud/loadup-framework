/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.lock;

import io.github.loadup.commons.json.ToStringAsJson;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Namespaced lock identity of one business resource.
 *
 * <p>The deployment namespace comes from the lock configuration, so two deployments sharing a
 * Redis instance never contend for the same key. No request, task or actor selects the namespace.
 */
public record LockKey(String business, String resourceId) {
    public LockKey {
        business = required(business, "business", 64);
        resourceId = required(resourceId, "resourceId", 512);
    }

    public static LockKey of(String business, String resourceId) {
        return new LockKey(business, resourceId);
    }

    static String required(String value, String field, int max) {
        if (value == null
                || value.isBlank()
                || value.length() > max
                || !value.equals(value.strip())
                || !StandardCharsets.UTF_8.newEncoder().canEncode(value)
                || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    field + " must contain 1 to " + max + " characters without surrounding whitespace or controls");
        }
        return value;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public String redisName(String namespace) {
        String prefix = "loadup:lock:" + encode(required(namespace, "namespace", 128)) + ":" + encode(business);
        return prefix + ":" + encode(resourceId);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(Map.of("scope", "deployment"));
    }
}
