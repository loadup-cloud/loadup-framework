package io.github.loadup.modules.file.web;

import io.github.loadup.modules.file.FileResourceService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "io.github.loadup.modules.file.autoconfigure.FileResourceAutoConfiguration")
@ConditionalOnBean(FileResourceService.class)
@ConditionalOnProperty(prefix = "loadup.file.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileResourceWebAutoConfiguration {
    @Bean
    public FileResourceController fileResourceController(FileResourceService service) {
        return new FileResourceController(service);
    }
}
