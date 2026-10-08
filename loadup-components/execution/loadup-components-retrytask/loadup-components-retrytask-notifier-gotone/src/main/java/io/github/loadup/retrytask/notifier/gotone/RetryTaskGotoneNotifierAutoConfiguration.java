package io.github.loadup.retrytask.notifier.gotone;

import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the gotone-backed retry task notifier.
 *
 * <p>Activates after the gotone engine has been configured and before the retry task binder so the
 * notifier is present when the binder collects all {@link RetryTaskNotifier} beans into its
 * failure filter. Only active when a {@link NotificationService} bean exists; the notifier is
 * additive: the default logging notifier of the retry task binder keeps working alongside it.
 */
@AutoConfiguration(
        afterName = "io.github.loadup.components.gotone.engine.GotoneEngineAutoConfiguration",
        beforeName = "io.github.loadup.retrytask.jobrunr.autoconfig.JobRunrRetryTaskAutoConfiguration")
@ConditionalOnClass({RetryTaskNotifier.class, NotificationService.class})
@ConditionalOnBean(NotificationService.class)
@EnableConfigurationProperties(RetryTaskNotifyProperties.class)
public class RetryTaskGotoneNotifierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "gotoneRetryTaskNotifier")
    public RetryTaskNotifier gotoneRetryTaskNotifier(
            NotificationService notificationService, RetryTaskNotifyProperties properties) {
        return new GotoneRetryTaskNotifier(notificationService, properties);
    }
}
