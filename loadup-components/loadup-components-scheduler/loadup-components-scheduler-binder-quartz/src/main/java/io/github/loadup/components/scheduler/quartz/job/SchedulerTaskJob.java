package io.github.loadup.components.scheduler.quartz.job;

/*-
 * #%L
 * loadup-components-scheduler-binder-quartz
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

/** Quartz job that executes a SchedulerTask. */
@Slf4j
public class SchedulerTaskJob implements Job {

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
