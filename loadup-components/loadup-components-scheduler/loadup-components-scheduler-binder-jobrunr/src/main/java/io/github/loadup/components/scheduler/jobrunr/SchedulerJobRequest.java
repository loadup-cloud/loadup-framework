package io.github.loadup.components.scheduler.jobrunr;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * JobRunr job payload of one scheduled run. Implemented as a mutable POJO because JobRunr
 * serializes the request with Jackson and needs a no-arg constructor plus bean properties.
 */
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
