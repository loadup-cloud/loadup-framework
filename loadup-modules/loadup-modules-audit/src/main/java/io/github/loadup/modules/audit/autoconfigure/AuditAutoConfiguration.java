package io.github.loadup.modules.audit.autoconfigure;

import io.github.loadup.modules.audit.AuditService;
import io.github.loadup.modules.audit.jdbc.JdbcAuditRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** Activates the JDBC audit center when a data source is available. */
@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public JdbcAuditRepository jdbcAuditRepository(DataSource dataSource) {
        return new JdbcAuditRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService(JdbcAuditRepository repository) {
        return new AuditService(repository);
    }
}
