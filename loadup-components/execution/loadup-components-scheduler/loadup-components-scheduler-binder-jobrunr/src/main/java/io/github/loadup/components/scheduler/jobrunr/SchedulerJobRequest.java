/*
 * #%L
 * Loadup Scheduler Binder JobRunr
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
package io.github.loadup.components.scheduler.jobrunr;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

public class SchedulerJobRequest implements JobRequest {

    private String taskName;
    private Map<String, String> args = new LinkedHashMap<>();

    /** Required by Jackson for deserialization. */
    public SchedulerJobRequest() {}

    public SchedulerJobRequest(String taskName, Map<String, String> args) {
        this.taskName = taskName;
        this.args = args == null ? new LinkedHashMap<>() : new LinkedHashMap<>(args);
    }

    @Override
    public Class<? extends JobRequestHandler> getJobRequestHandler() {
        return SchedulerJobRequestHandler.class;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args == null ? new LinkedHashMap<>() : new LinkedHashMap<>(args);
    }
}
