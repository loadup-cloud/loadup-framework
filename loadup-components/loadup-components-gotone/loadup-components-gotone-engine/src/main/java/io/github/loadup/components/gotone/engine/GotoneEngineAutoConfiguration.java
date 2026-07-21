package io.github.loadup.components.gotone.engine;

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.GotoneTemplate;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.template.TemplateRenderer;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GotoneEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NotificationChannelManager notificationChannelManager(List<GotoneProvider> providers) {
        return new NotificationChannelManager(providers);
    }

    @Bean
    @ConditionalOnMissingBean(GotoneTemplate.class)
    public GotoneTemplate gotoneTemplate(
            NotificationChannelManager channelManager,
            Optional<ChannelConfigProvider> channelConfigProvider,
            Optional<TemplateRenderer> templateRenderer,
            Optional<RecordHandler> recordHandler) {
        return new DefaultGotoneTemplate(channelManager, channelConfigProvider, templateRenderer, recordHandler);
    }
}
