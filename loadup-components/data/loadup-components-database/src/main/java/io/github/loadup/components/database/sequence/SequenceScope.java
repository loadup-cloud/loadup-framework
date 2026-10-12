/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import java.time.LocalDate;

/** A shared allocation namespace; the maximum is immutable once initialized. */
public final class SequenceScope {
    private static final java.util.regex.Pattern NAME = java.util.regex.Pattern.compile("[A-Za-z0-9_-]{1,120}");
    private final String name;
    private final LocalDate date;
    private final long maximum;
    private final String key;

    public SequenceScope(String name, LocalDate date, long maximum) {
        if (name == null || !NAME.matcher(name).matches() || maximum < 1 || maximum == Long.MAX_VALUE)
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        this.name = name;
        this.date = date;
        this.maximum = maximum;
        this.key = name + ":" + (date == null ? "persistent" : date.toString());
    }

    public String name() {
        return name;
    }

    public LocalDate date() {
        return date;
    }

    public long maximum() {
        return maximum;
    }

    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SequenceScope value && maximum == value.maximum && key.equals(value.key);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, maximum);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
