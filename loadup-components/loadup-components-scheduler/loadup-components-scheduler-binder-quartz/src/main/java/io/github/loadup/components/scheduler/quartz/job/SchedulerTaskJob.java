package io.github.loadup.components.scheduler.quartz.job;

/*-
 * #%L
 * loadup-components-scheduler-binder-quartz
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Method;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz job that executes a SchedulerTask.
 */
public class SchedulerTaskJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SchedulerTaskJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SchedulerTask task =
                (SchedulerTask) context.getJobDetail().getJobDataMap().get("schedulerTask");

        if (task == null) {
            log.error("SchedulerTask not found in job data");
            return;
        }

        String taskName = task.getTaskName();
        Method method = task.getMethod();
        Object targetBean = task.getTargetBean();

        try {
            log.debug("Executing Quartz job for task: {}", taskName);
            long startTime = System.currentTimeMillis();

            method.setAccessible(true); // Allow access to methods in nested classes
            method.invoke(targetBean);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Task '{}' executed successfully in {} ms", taskName, duration);

        } catch (Exception e) {
            log.error("Error executing task: {}", taskName, e);
            throw new JobExecutionException("Failed to execute task: " + taskName, e);
        }
    }
}
