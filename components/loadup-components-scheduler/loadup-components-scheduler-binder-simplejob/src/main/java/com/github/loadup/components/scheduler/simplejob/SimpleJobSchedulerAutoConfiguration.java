/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.simplejob;

/*-
 * #%L
 * loadup-components-scheduler-binder-simplejob
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.config.SchedulerAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

/**
 * Auto-configuration for SimpleJob scheduler binder.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureBefore(SchedulerAutoConfiguration.class)
@ConditionalOnProperty(
    prefix = "loadup.scheduler",
    name = "type",
    havingValue = "simplejob",
    matchIfMissing = true
)
public class SimpleJobSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SchedulerBinder.class)
    public SchedulerBinder simpleJobSchedulerBinder(TaskScheduler taskScheduler) {
        log.info("Creating SimpleJob scheduler binder");
        return new SimpleJobSchedulerBinder(taskScheduler);
    }
}

