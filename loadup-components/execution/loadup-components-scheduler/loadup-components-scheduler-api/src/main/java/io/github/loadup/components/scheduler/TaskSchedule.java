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
package io.github.loadup.components.scheduler;

import java.util.Locale;
import org.springframework.scheduling.support.CronExpression;

/** A task name and a portable, six-field cron expression. */
public record TaskSchedule(String name, String cron) {
    public TaskSchedule {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid scheduler task name: " + name);
        }
        if (cron == null) {
            throw invalidCron(name);
        }
        var fields = cron.strip().toUpperCase(Locale.ROOT).split("\\s+");
        if (fields.length != 6 || !fields[5].matches("[*?]|[A-Z]{3}(?:-[A-Z]{3})?(?:,[A-Z]{3}(?:-[A-Z]{3})?)*")) {
            throw invalidCron(name);
        }
        // Quartz requires one unspecified day field. Named weekdays avoid differing numeric conventions.
        if (fields[5].equals("*") || fields[5].equals("?")) {
            fields[5] = "?";
            if (fields[3].equals("?")) {
                fields[3] = "*";
            }
        } else if (fields[3].equals("*") || fields[3].equals("?")) {
            fields[3] = "?";
        } else {
            throw invalidCron(name);
        }
        cron = String.join(" ", fields);
        if (!CronExpression.isValidExpression(cron)) {
            throw invalidCron(name);
        }
    }

    static boolean isValidName(String name) {
        return name != null && name.matches("[A-Za-z][A-Za-z0-9_.-]{0,63}");
    }

    private static IllegalArgumentException invalidCron(String name) {
        return new IllegalArgumentException("Invalid scheduler cron for task '" + name
                + "': use six fields, named weekdays, and constrain at most one day field");
    }
}
