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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.storage.RecurringJobsResult;
import org.junit.jupiter.api.Test;

class RecurringJobCacheInvalidatorTest {
    private static final Instant NOW = Instant.parse("2026-10-18T00:00:00Z");

    @Test
    void keepsUnchangedCursorsWhenAnUnrelatedTaskIsAdded() {
        var previous = job("existing", NOW, "*/5 * * * * *", "Asia/Shanghai");
        var updated = new RecurringJobsResult(List.of(
                job("existing", NOW, "*/5 * * * * *", "Asia/Shanghai"),
                job("new", NOW, "*/5 * * * * *", "Asia/Shanghai")));
        Map<String, Instant> runs = new HashMap<>(Map.of("existing", NOW.plusSeconds(5)));
        assertThat(RecurringJobCacheInvalidator.refresh(runs, List.of(previous), updated))
                .isSameAs(updated);
        assertThat(runs).containsExactlyEntriesOf(Map.of("existing", NOW.plusSeconds(5)));
    }

    @Test
    void clearsCursorsForChangedSchedulesZonesAndRecreatedTasks() {
        var previous = List.of(
                job("cron", NOW, "0 0 0 1 1 *", "Asia/Shanghai"),
                job("zone", NOW, "*/5 * * * * *", "Asia/Shanghai"),
                job("recreated", NOW, "*/5 * * * * *", "Asia/Shanghai"));
        var updated = new RecurringJobsResult(List.of(
                job("cron", NOW, "*/5 * * * * *", "Asia/Shanghai"),
                job("zone", NOW, "*/5 * * * * *", "Europe/London"),
                job("recreated", NOW.plusSeconds(1), "*/5 * * * * *", "Asia/Shanghai")));
        Map<String, Instant> runs = new HashMap<>(Map.of("cron", NOW, "zone", NOW, "recreated", NOW));
        RecurringJobCacheInvalidator.refresh(runs, previous, updated);
        assertThat(runs).isEmpty();
    }

    @Test
    void removesDeletedCursorsAndLeavesEmptyInitialStateEmpty() {
        Map<String, Instant> runs = new HashMap<>(Map.of("deleted", NOW));
        RecurringJobCacheInvalidator.refresh(
                runs, List.of(job("deleted", NOW, "*/5 * * * * *", "Asia/Shanghai")), new RecurringJobsResult());
        assertThat(runs).isEmpty();
        RecurringJobCacheInvalidator.refresh(runs, List.of(), new RecurringJobsResult());
        assertThat(runs).isEmpty();
    }

    private static RecurringJob job(String id, Instant createdAt, String cron, String zone) {
        var job = mock(RecurringJob.class);
        when(job.getId()).thenReturn(id);
        when(job.getCreatedAt()).thenReturn(createdAt);
        when(job.getScheduleExpression()).thenReturn(cron);
        when(job.getZoneId()).thenReturn(zone);
        return job;
    }
}
