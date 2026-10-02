package io.github.loadup.modules.file.autoconfigure;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.modules.file.FileResourceRepository;
import io.github.loadup.modules.file.FileResourceService;
import io.github.loadup.modules.file.jdbc.JdbcFileResourceRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** Creates file metadata services when both JDBC and a DFS binder are present. */
@AutoConfiguration(afterName = {"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration"})
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean(DfsService.class)
@ConditionalOnProperty(prefix = "loadup.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileResourceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(FileResourceRepository.class)
    public FileResourceRepository fileResourceRepository(DataSource dataSource) {
        return new JdbcFileResourceRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean(FileResourceService.class)
    public FileResourceService fileResourceService(FileResourceRepository repository, DfsService dfs) {
        return new FileResourceService(repository, dfs);
    }
}
