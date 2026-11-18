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
package io.github.loadup.components.task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;

final class TaskSql {
    enum Query {
        UPSERT,
        LOCK_BY_BUSINESS,
        RECOVER_EXPIRED,
        CLAIM_CANDIDATE,
        CLAIM,
        LOCK_OWNER,
        COMPLETE,
        FAIL,
        FIND_BY_ID,
        FIND_BY_BUSINESS,
        FIND_FAILURES,
        CANCEL,
        RESCHEDULE,
        RETRY_FAILED,
        INSERT_OPERATION,
        COUNT_PENDING,
        COUNT_FAILED,
        OLDEST_PENDING
    }

    private static final Pattern HEADER = Pattern.compile("(?m)^-- name: ([A-Z_]+)\\R");
    private final Map<Query, String> statements;

    TaskSql(TaskProperties properties) {
        String dialect = properties.getDbType().name().toLowerCase(Locale.ROOT);
        var resource = new ClassPathResource("db/task/" + dialect + "/queries.sql");
        try (var input = resource.getInputStream()) {
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            var headers = HEADER.matcher(content).results().toList();
            var queries = new EnumMap<Query, String>(Query.class);
            for (int i = 0; i < headers.size(); i++) {
                var header = headers.get(i);
                Query key = Query.valueOf(header.group(1));
                int start = header.end();
                int end = i + 1 < headers.size() ? headers.get(i + 1).start() : content.length();
                String sql = content.substring(start, end).strip().replace("{{prefix}}", properties.getTablePrefix());
                if (sql.isEmpty() || queries.putIfAbsent(key, sql) != null) {
                    throw new IllegalStateException("Empty or duplicate task SQL statement: " + key);
                }
            }
            if (queries.size() != Query.values().length) {
                throw new IllegalStateException("Incomplete task SQL set: " + dialect);
            }
            statements = Map.copyOf(queries);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot load task SQL set: " + dialect, failure);
        }
    }

    String get(Query query) {
        return statements.get(query);
    }
}
