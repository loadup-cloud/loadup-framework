package com.github.loadup.components.scheduler.binding;

/*-
 * #%L
 * loadup-components-scheduler-api
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

import com.github.loadup.components.scheduler.binder.SchedulerBinder;
import com.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import com.github.loadup.framework.api.binding.AbstractBinding;
import com.github.loadup.framework.api.context.BindingContext;
import lombok.extern.slf4j.Slf4j;

/** Default implementation of SchedulerBinding that delegates to a SchedulerBinder. */
@Slf4j
public class DefaultSchedulerBinding
    extends AbstractBinding<SchedulerBinder<?, SchedulerBindingCfg>, SchedulerBindingCfg>
    implements SchedulerBinding {

  @Override
  public boolean registerTask(SchedulerTask task) {
    log.debug("Registering task: {}", task.getTaskName());
    return getBinder().registerTask(task);
  }

  @Override
  public boolean unregisterTask(String taskName) {
    log.debug("Unregistering task: {}", taskName);
    return getBinder().unregisterTask(taskName);
  }

  @Override
  public boolean pauseTask(String taskName) {
    log.debug("Pausing task: {}", taskName);
    return getBinder().pauseTask(taskName);
  }

  @Override
  public boolean resumeTask(String taskName) {
    log.debug("Resuming task: {}", taskName);
    return getBinder().resumeTask(taskName);
  }

  @Override
  public boolean triggerTask(String taskName) {
    log.debug("Triggering task: {}", taskName);
    return getBinder().triggerTask(taskName);
  }

  @Override
  public boolean updateTaskCron(String taskName, String cron) {
    log.debug("Updating task {} cron to: {}", taskName, cron);
    return getBinder().updateTaskCron(taskName, cron);
  }

  @Override
  public boolean taskExists(String taskName) {
    return getBinder().taskExists(taskName);
  }

  public void afterInit() {
    log.info("Initializing scheduler binding");
    getBinder().binderInit();
  }
}
