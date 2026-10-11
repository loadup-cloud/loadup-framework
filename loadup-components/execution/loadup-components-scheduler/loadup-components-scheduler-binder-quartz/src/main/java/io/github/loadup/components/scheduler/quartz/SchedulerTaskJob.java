/*
 * #%L
 * Loadup Scheduler Quartz Binder
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
package io.github.loadup.components.scheduler.quartz;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import java.util.Map;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SchedulerTaskJob extends QuartzJobBean implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            String taskName = context.getMergedJobDataMap().getString("taskName");
            @SuppressWarnings("unchecked")
            Map<String, String> args =
                    (Map<String, String>) context.getMergedJobDataMap().get("args");
            SchedulerProcessorRegistry processorRegistry = applicationContext.getBean(SchedulerProcessorRegistry.class);
            SchedulerProcessor processor = processorRegistry.getProcessor(taskName);
            processor.process(new SchedulerContext(taskName, args == null ? Map.of() : args));
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
