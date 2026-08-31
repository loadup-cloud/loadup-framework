/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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

package io.github.loadup.retrytask.jobrunr;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * JobRunr job payload. Implemented as a mutable POJO because JobRunr serializes the request with
 * Jackson and needs a no-arg constructor plus bean properties.
 */
public class RetryTaskJobRequest implements JobRequest {

    private String bizType;
    private String bizId;
    private Map<String, String> args = new LinkedHashMap<>();
    private Integer maxRetries;

    /** Required by Jackson for deserialization. */
    public RetryTaskJobRequest() {}

    public RetryTaskJobRequest(String bizType, String bizId, Map<String, String> args, Integer maxRetries) {
        this.bizType = bizType;
        this.bizId = bizId;
        this.args = args == null ? new LinkedHashMap<>() : new LinkedHashMap<>(args);
        this.maxRetries = maxRetries;
    }

    @Override
    public Class<? extends JobRequestHandler> getJobRequestHandler() {
        return RetryTaskJobRequestHandler.class;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args == null ? new LinkedHashMap<>() : new LinkedHashMap<>(args);
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
