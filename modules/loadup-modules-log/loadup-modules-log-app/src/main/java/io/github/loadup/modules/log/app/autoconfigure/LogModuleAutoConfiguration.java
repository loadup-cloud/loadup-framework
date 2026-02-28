package io.github.loadup.modules.log.app.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.modules.log.infrastructure.aspect.OperationLogAspect;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import io.github.loadup.modules.log.infrastructure.repository.AuditLogGatewayImpl;
import io.github.loadup.modules.log.infrastructure.repository.OperationLogGatewayImpl;
import io.github.loadup.modules.log.app.service.AuditLogService;
import io.github.loadup.modules.log.app.service.OperationLogService;
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

