package io.github.loadup.modules.notification.autoconfigure;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.modules.notification.InboxRepository;
import io.github.loadup.modules.notification.InboxService;
import io.github.loadup.modules.notification.gotone.InAppChannelProvider;
import io.github.loadup.modules.notification.jdbc.JdbcInboxRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        beforeName = "io.github.loadup.components.gotone.engine.GotoneEngineAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.notification", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(InboxRepository.class)
    public InboxRepository inboxRepository(DataSource dataSource) {
        return new JdbcInboxRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean(InboxService.class)
    public InboxService inboxService(InboxRepository repository) { return new InboxService(repository); }

    @Bean
    public NotificationChannelProvider inAppNotificationChannelProvider(InboxService inbox) {
        return new InAppChannelProvider(inbox);
    }
}
