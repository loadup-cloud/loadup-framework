package io.github.loadup.modules.config.domain.event;

import io.github.loadup.modules.config.domain.enums.ChangeType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published whenever a config item is created, updated, or deleted.
 *
 * <p>Listeners (e.g. {@code ConfigLocalCache}) subscribe via Spring's
 * {@code @EventListener} to react to these changes.
 */
@Getter
public class ConfigChangedEvent extends ApplicationEvent {

    private final String configKey;
    private final String newValue;
    private final ChangeType changeType;
    private final String operator;

    public ConfigChangedEvent(Object source, String configKey, String newValue,
            ChangeType changeType, String operator) {
        super(source);
        this.configKey = configKey;
        this.newValue = newValue;
        this.changeType = changeType;
        this.operator = operator;
    }
}

