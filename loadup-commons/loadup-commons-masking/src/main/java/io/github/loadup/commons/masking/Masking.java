/*
 * #%L
 * LoadUp Common Masking
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
package io.github.loadup.commons.masking;

import java.util.Objects;

/** Stateless, Unicode code-point-aware rules for API output, explicit logs and exports. */
public final class Masking {
    private static final String HIDDEN = "******";

    private Masking() {}

    public static String mask(String value, MaskType type) {
        Objects.requireNonNull(type, "mask type");
        if (type == MaskType.CUSTOM) throw new IllegalArgumentException("CUSTOM requires explicit prefix and suffix");
        if (value == null || value.isEmpty()) return value;
        return switch (type) {
            case PHONE -> value.matches("\\+?[0-9]{8,15}") ? keep(value, 3, 4) : HIDDEN;
            case EMAIL -> email(value);
            case ID_CARD -> value.matches("[0-9]{17}[0-9Xx]|[0-9]{15}") ? keep(value, 3, 4) : HIDDEN;
            case BANK_CARD -> value.matches("[0-9]{13,19}") ? keep(value, 0, 4) : HIDDEN;
            case NAME ->
                value.isBlank() || value.codePoints().anyMatch(Character::isISOControl) ? HIDDEN : keep(value, 1, 0);
            case FULL -> HIDDEN;
            case CUSTOM -> throw new IllegalArgumentException("CUSTOM requires explicit prefix and suffix");
        };
    }

    public static String keep(String value, int prefix, int suffix) {
        if (prefix < 0 || suffix < 0 || prefix > 32 || suffix > 32)
            throw new IllegalArgumentException("Invalid masking lengths");
        if (value == null || value.isEmpty()) return value;
        int length = value.codePointCount(0, value.length());
        if (length <= prefix + suffix || value.codePoints().anyMatch(Character::isISOControl)) return HIDDEN;
        return value.substring(0, value.offsetByCodePoints(0, prefix))
                + "*".repeat(length - prefix - suffix)
                + value.substring(value.offsetByCodePoints(0, length - suffix));
    }

    private static String email(String value) {
        if (!value.matches("[^\\s@\\p{Cntrl}]+@[^\\s@\\p{Cntrl}]+\\.[^\\s@\\p{Cntrl}.]+")) return HIDDEN;
        int at = value.indexOf('@');
        String local = value.substring(0, at);
        int length = local.codePointCount(0, local.length());
        return (length == 1 ? "*" : keep(local, 1, 0)) + value.substring(at);
    }
}
