package io.github.loadup.components.scheduler.xxljob.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-xxljob
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

import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * XXL-Job scheduler binder implementation.
 * Note: XXL-Job uses annotation-based task registration (@XxlJob),
 * so this binder provides limited runtime management capabilities.
 */
@Slf4j
public class XxlJobSchedulerBinder implements SchedulerBinder {

    private static final String                     BINDER_NAME  = "xxljob";
    private final        Map<String, SchedulerTask> taskRegistry = new ConcurrentHashMap<>();

  @Override
  public String getBinderType() {
    return BINDER_NAME;
  }

    @Override
    public void injectConfigs(String name, BaseBinderCfg binderCfg, BaseBindingCfg bindingCfg) {

    }
  @Override
  public void binderInit() {
        log.info("Initializing XXL-Job scheduler binder");
        log.warn("XXL-Job uses annotation-based registration. Runtime task management is limited.");
    }

  @Override
  public void binderDestroy() {
        log.info("Destroying XXL-Job scheduler binder");
        taskRegistry.clear();
    }



    @Override
    public boolean registerTask(SchedulerTask task) {
        // XXL-Job uses @XxlJob annotation for registration
        // We store the task for reference but cannot dynamically register
        String taskName = task.getTaskName();
        taskRegistry.put(taskName, task);
        log.info("Task '{}' registered in local registry. Note: XXL-Job requires @XxlJob annotation", taskName);
        log.warn("XXL-Job tasks must be registered via admin console and use @XxlJob annotation");
        return true;
    }

    @Override
    public boolean unregisterTask(String taskName) {
        SchedulerTask removed = taskRegistry.remove(taskName);
        if (removed != null) {
            log.info("Task '{}' removed from local registry", taskName);
            log.warn("XXL-Job tasks must be managed via admin console");
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseTask(String taskName) {
        log.warn("XXL-Job task pause must be done via admin console");
        return false;
    }

    @Override
    public boolean resumeTask(String taskName) {
        log.warn("XXL-Job task resume must be done via admin console");
        return false;
    }

    @Override
    public boolean triggerTask(String taskName) {
        log.warn("XXL-Job task trigger must be done via admin console");
        return false;
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        log.warn("XXL-Job task cron update must be done via admin console");
        return false;
    }

    @Override
    public boolean taskExists(String taskName) {
        return taskRegistry.containsKey(taskName);
    }

  @Override
  public void destroy() throws Exception {}
}

