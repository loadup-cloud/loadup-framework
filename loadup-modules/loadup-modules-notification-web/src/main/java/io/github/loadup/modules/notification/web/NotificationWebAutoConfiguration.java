package io.github.loadup.modules.notification.web;

import io.github.loadup.modules.notification.InboxService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "io.github.loadup.modules.notification.autoconfigure.NotificationAutoConfiguration")
@ConditionalOnBean(InboxService.class)
@ConditionalOnProperty(prefix = "loadup.notification.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationWebAutoConfiguration {
    @Bean
    public NotificationController notificationController(InboxService inbox) {
        return new NotificationController(inbox);
    }
}
