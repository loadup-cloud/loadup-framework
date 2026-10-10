/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import io.github.loadup.commons.masking.Masking;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import tools.jackson.databind.json.JsonMapper;

/** Bounded JSON diagnostics; independent of HTTP serialization and never calls bean getters. */
public final class ToStringAsJson {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int MAX_DEPTH = 6;
    private static final int MAX_ITEMS = 32;
    private static final int MAX_STRING = 256;

    private ToStringAsJson() {}

    public static String reflectionToString(Object value) {
        try {
            return MAPPER.writeValueAsString(project(value, 0, new IdentityHashMap<>(), new Budget()));
        } catch (RuntimeException failure) {
            return "{\"diagnostic\":\"unavailable\"}";
        }
    }

    private static Object project(Object value, int depth, IdentityHashMap<Object, Boolean> path, Budget budget) {
        if (value == null) return null;
        if (--budget.remaining < 0) return "<truncated>";
        if (value instanceof CharSequence text) return bounded(text.toString());
        if (value instanceof Boolean
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger
                || value instanceof BigDecimal) return value;
        if (value instanceof Double n) return Double.isFinite(n) ? n : n.toString();
        if (value instanceof Float n) return Float.isFinite(n) ? n : n.toString();
        if (value instanceof Character
                || value instanceof UUID
                || value instanceof Currency
                || value instanceof TemporalAccessor) return bounded(value.toString());
        if (value instanceof Enum<?> e) return e.name();
        if (depth >= MAX_DEPTH) return "<depth-limit>";
        if (path.put(value, Boolean.TRUE) != null) return "<cycle>";
        try {
            if (value instanceof Optional<?> optional) return project(optional.orElse(null), depth + 1, path, budget);
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> result = new LinkedHashMap<>();
                int count = 0;
                for (var entry : map.entrySet()) {
                    if (count++ >= MAX_ITEMS || budget.remaining <= 0) {
                        result.put("$truncated", true);
                        break;
                    }
                    String key = entry.getKey() instanceof String text ? text : "<non-string-key-" + count + ">";
                    result.put(
                            bounded(key),
                            sensitive(key) ? "******" : project(entry.getValue(), depth + 1, path, budget));
                }
                return result;
            }
            if (value instanceof Collection<?> collection) {
                List<Object> result = new ArrayList<>();
                int count = 0;
                for (Object item : collection) {
                    if (count++ >= MAX_ITEMS || budget.remaining <= 0) {
                        result.add("<truncated>");
                        break;
                    }
                    result.add(project(item, depth + 1, path, budget));
                }
                return result;
            }
            if (value.getClass().isArray()) {
                if (value instanceof byte[] || value instanceof char[]) return "<binary-or-secret>";
                List<Object> result = new ArrayList<>();
                int count = Math.min(Array.getLength(value), MAX_ITEMS);
                for (int i = 0; i < count && budget.remaining > 0; i++)
                    result.add(project(Array.get(value, i), depth + 1, path, budget));
                if (Array.getLength(value) > result.size()) result.add("<truncated>");
                return result;
            }
            if (!value.getClass().getPackageName().startsWith("io.github.loadup."))
                return "<" + value.getClass().getSimpleName() + ">";
            Map<String, Object> result = new LinkedHashMap<>();
            for (Class<?> type = value.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())
                            || field.isSynthetic()
                            || result.containsKey(field.getName())) continue;
                    if (budget.remaining <= 0 || result.size() >= MAX_ITEMS) {
                        result.put("$truncated", true);
                        return result;
                    }
                    JsonProperty property = field.getAnnotation(JsonProperty.class);
                    if (field.isAnnotationPresent(DiagnosticHidden.class)
                            || sensitive(field.getName())
                            || property != null && property.access() == JsonProperty.Access.WRITE_ONLY) {
                        result.put(field.getName(), "******");
                        continue;
                    }
                    if (!field.trySetAccessible()) {
                        result.put(field.getName(), "<inaccessible>");
                        continue;
                    }
                    Object raw = field.get(value);
                    Masked masked = field.getAnnotation(Masked.class);
                    if (masked != null && raw instanceof String text) {
                        raw = masked.value() == MaskType.CUSTOM
                                ? Masking.keep(text, masked.prefix(), masked.suffix())
                                : Masking.mask(text, masked.value());
                    }
                    result.put(field.getName(), project(raw, depth + 1, path, budget));
                }
            }
            return result;
        } catch (IllegalAccessException | RuntimeException failure) {
            return "<unavailable>";
        } finally {
            path.remove(value);
        }
    }

    private static boolean sensitive(String name) {
        String normalized = name.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("privatekey")
                || normalized.contains("credential")
                || normalized.equals("authorization")
                || normalized.equals("apikey")
                || normalized.equals("verificationcode")
                || normalized.equals("smscode")
                || normalized.equals("emailcode")
                || normalized.equals("pwd");
    }

    private static String bounded(String text) {
        return text.length() <= MAX_STRING ? text : text.substring(0, MAX_STRING) + "<truncated>";
    }

    private static final class Budget {
        int remaining = 128;
    }
}
