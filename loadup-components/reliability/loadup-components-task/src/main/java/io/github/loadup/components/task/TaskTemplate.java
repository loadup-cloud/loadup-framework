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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskTemplate {
    /** Enqueues work in the caller transaction. Matching queued work returns its existing ID and retains its schedule. */
    String submit(TaskSubmission submission);

    /** Returns a retained queue record. Successful acknowledgement removes the record. */
    Optional<TaskExecution> find(String id);

    Optional<TaskExecution> find(String type, String biz);

    List<TaskExecution> failures(int limit);

    boolean cancel(String id);

    void reschedule(String id, LocalDateTime executeAt);

    void retryFailed(String id, String operator, String reason);
}
