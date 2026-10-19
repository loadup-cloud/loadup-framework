/*
 * #%L
 * LoadUp Components PowerJob
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
package io.github.loadup.components.powerjob;

import io.github.loadup.commons.context.ContextKey;

/** Execution identifiers only; job payloads and authentication are not propagated implicitly. */
public final class PowerJobExecutionContext {
    public static final ContextKey<Long> JOB_ID = new ContextKey<>("powerjob.jobId", Long.class);
    public static final ContextKey<Long> INSTANCE_ID = new ContextKey<>("powerjob.instanceId", Long.class);
    public static final ContextKey<String> TASK_ID = new ContextKey<>("powerjob.taskId", String.class);
    public static final ContextKey<Integer> RETRY_TIMES = new ContextKey<>("powerjob.retryTimes", Integer.class);

    private PowerJobExecutionContext() {}
}
