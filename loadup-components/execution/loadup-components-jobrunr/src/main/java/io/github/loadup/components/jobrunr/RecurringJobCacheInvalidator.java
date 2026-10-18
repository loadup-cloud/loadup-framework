/*
 * #%L
 * LoadUp Components JobRunr
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
package io.github.loadup.components.jobrunr;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.storage.RecurringJobsResult;

/** Invalidates only stale schedule cursors when a JobRunr server reloads recurring definitions. */
public final class RecurringJobCacheInvalidator {
    private RecurringJobCacheInvalidator() {}

    public static RecurringJobsResult refresh(
            Map<String, Instant> runs, List<RecurringJob> previous, RecurringJobsResult updated) {
        Map<String, RecurringJob> byId =
                previous.stream().collect(Collectors.toMap(RecurringJob::getId, Function.identity()));
        Set<String> unchanged = updated.stream()
                .filter(job -> sameDefinition(byId.get(job.getId()), job))
                .map(RecurringJob::getId)
                .collect(Collectors.toSet());
        runs.keySet().retainAll(unchanged);
        return updated;
    }

    private static boolean sameDefinition(RecurringJob previous, RecurringJob current) {
        return previous != null
                && previous.getCreatedAt().equals(current.getCreatedAt())
                && previous.getScheduleExpression().equals(current.getScheduleExpression())
                && previous.getZoneId().equals(current.getZoneId());
    }
}
