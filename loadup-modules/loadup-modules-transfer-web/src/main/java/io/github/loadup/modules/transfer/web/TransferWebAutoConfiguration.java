package io.github.loadup.modules.transfer.web;

import io.github.loadup.modules.transfer.TransferTaskService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "io.github.loadup.modules.transfer.autoconfigure.TransferAutoConfiguration")
@ConditionalOnBean(TransferTaskService.class)
@ConditionalOnProperty(prefix = "loadup.transfer.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransferWebAutoConfiguration {
    @Bean
    public TransferTaskController transferTaskController(TransferTaskService service) {
        return new TransferTaskController(service);
    }
}
