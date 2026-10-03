package io.github.loadup.modules.transfer.autoconfigure;

import io.github.loadup.modules.file.FileResourceService;
import io.github.loadup.modules.transfer.TransferHandler;
import io.github.loadup.modules.transfer.TransferRepository;
import io.github.loadup.modules.transfer.TransferTaskProperties;
import io.github.loadup.modules.transfer.TransferTaskService;
import io.github.loadup.modules.transfer.jdbc.JdbcTransferRepository;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(afterName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "io.github.loadup.modules.file.autoconfigure.FileResourceAutoConfiguration",
        "io.github.loadup.retrytask.jobrunr.autoconfig.JobRunrRetryTaskAutoConfiguration"})
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean({FileResourceService.class, RetryTaskFacade.class})
@ConditionalOnProperty(prefix = "loadup.transfer", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TransferTaskProperties.class)
public class TransferAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TransferRepository.class)
    public TransferRepository transferRepository(DataSource dataSource) {
        return new JdbcTransferRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean(TransferTaskService.class)
    public TransferTaskService transferTaskService(TransferRepository repository, FileResourceService files,
            RetryTaskFacade retryTasks, List<TransferHandler> handlers, TransferTaskProperties properties) {
        return new TransferTaskService(repository, files, retryTasks, handlers, properties);
    }
}
