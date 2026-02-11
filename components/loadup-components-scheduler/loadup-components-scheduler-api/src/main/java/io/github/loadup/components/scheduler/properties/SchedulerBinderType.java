package io.github.loadup.components.scheduler.properties;

/*-
 * #%L
 * Loadup Scheduler Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SchedulerBinderType {
    SIMPLE_JOB("simplejob"),
    QUARTZ("quartz"),
    XXL_JOB("xxljob"),
    POWER_JOB("powerjob"),
    ;

    private final String value;

    SchedulerBinderType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SchedulerBinderType fromString(String value) {
        if (value == null) return null;

        for (SchedulerBinderType type : SchedulerBinderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown binder type: " + value);
    }
}
