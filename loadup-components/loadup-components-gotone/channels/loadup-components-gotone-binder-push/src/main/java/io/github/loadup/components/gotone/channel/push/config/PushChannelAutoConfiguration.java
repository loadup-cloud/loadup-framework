package io.github.loadup.components.gotone.channel.push.config;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.push.FcmPushConfig;
import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Firebase Cloud Messaging push binder.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.gotone.binder.push.fcm",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(FcmPushConfig.class)
public class PushChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "fcmPushProvider")
    public NotificationChannelProvider fcmPushProvider(FcmPushConfig config) {
        return new FcmPushProvider(config.getServerKey(), config.getProjectId());
    }
}
