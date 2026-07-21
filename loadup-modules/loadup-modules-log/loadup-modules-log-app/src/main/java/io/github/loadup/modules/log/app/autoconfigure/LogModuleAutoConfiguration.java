package io.github.loadup.modules.log.app.autoconfigure;

/*-
 * #%L
 * Loadup Modules Log App
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.modules.log.infrastructure.aspect.OperationLogAspect;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@AutoConfiguration
@EnableAsync
@ComponentScan(basePackages = "io.github.loadup.modules.log")
@MapperScan("io.github.loadup.modules.log.infrastructure.mapper")
public class LogModuleAutoConfiguration {

    /**
     * Dedicated thread pool for async log writing.
     * Named "logExecutor" to match {@code @Async("logExecutor")}.
     */
    @Bean("logExecutor")
    @ConditionalOnMissingBean(name = "logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationLogAspect operationLogAspect(LogAsyncWriter logAsyncWriter, ObjectMapper objectMapper) {
        return new OperationLogAspect(logAsyncWriter, objectMapper);
    }
}
