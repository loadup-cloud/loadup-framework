package io.github.loadup.components.configcenter.binding.impl;

import io.github.loadup.components.configcenter.binder.ConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.ConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.components.configcenter.model.ConfigChangeType;
import io.github.loadup.framework.api.binding.AbstractBinding;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Default implementation of {@link ConfigCenterBinding}.
 *
 * <p>Delegates actual reads and writes to the underlying {@link ConfigCenterBinder},
 * and publishes change events to the Spring application event bus so that mechanisms
 * such as {@code @EnableConfigAutoRefresh} can react to configuration changes.
 */
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultConfigCenterBinding extends AbstractBinding<ConfigCenterBinder, ConfigCenterBindingCfg>
        implements ConfigCenterBinding, ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    // ── Read ────────────────────────────────────────────────────────────────

    @Override
    public Optional<String> getConfig(String dataId) {
        return getConfig(dataId, resolveGroup(null));
    }

    @Override
    public Optional<String> getConfig(String dataId, String group) {
        ConfigCenterBinder binder = getBinder();
        return binder.getConfig(dataId, group, bindingCfg);
    }

    // ── Publish ─────────────────────────────────────────────────────────────

    @Override
    public void publishConfig(String dataId, String content) {
        publishConfig(dataId, resolveGroup(null), content);
    }

    @Override
    public void publishConfig(String dataId, String group, String content) {
        ConfigCenterBinder binder = getBinder();
        boolean success = binder.publishConfig(dataId, group, content, bindingCfg);
        if (!success) {
            log.warn("[ConfigCenter] publishConfig failed: dataId={}, group={}", dataId, group);
        }
    }

    // ── Remove ──────────────────────────────────────────────────────────────

    @Override
    public boolean removeConfig(String dataId) {
        return removeConfig(dataId, resolveGroup(null));
    }

    @Override
    public boolean removeConfig(String dataId, String group) {
        ConfigCenterBinder binder = getBinder();
        return binder.removeConfig(dataId, group, bindingCfg);
    }

    // ── Listen ──────────────────────────────────────────────────────────────

    @Override
    public void addListener(String dataId, ConfigChangeListener listener) {
        addListener(dataId, resolveGroup(null), listener);
    }

    @Override
    public void addListener(String dataId, String group, ConfigChangeListener listener) {
        ConfigCenterBinder binder = getBinder();
        // Wrap the original listener: invoke user callback first, then publish Spring event
        ConfigChangeListener wrapped = event -> {
            listener.onChange(event);
            publishChangeEvent(event);
        };
        binder.addListener(dataId, group, wrapped, bindingCfg);
    }

    @Override
    public void removeListener(String dataId, ConfigChangeListener listener) {
        removeListener(dataId, resolveGroup(null), listener);
    }

    @Override
    public void removeListener(String dataId, String group, ConfigChangeListener listener) {
        ConfigCenterBinder binder = getBinder();
        binder.removeListener(dataId, group, listener, bindingCfg);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private String resolveGroup(String group) {
        if (group != null && !group.isBlank()) {
            return group;
        }
        if (bindingCfg != null && bindingCfg.getGroup() != null && !bindingCfg.getGroup().isBlank()) {
            return bindingCfg.getGroup();
        }
        // fallback: read defaultGroup from binderCfg
        ConfigCenterBinder binder = getBinder();
        if (binder instanceof AbstractConfigCenterBinder<?, ?> abstractBinder) {
            ConfigCenterBinderCfg cfg = abstractBinder.getBinderCfg();
            if (cfg != null && cfg.getDefaultGroup() != null) {
                return cfg.getDefaultGroup();
            }
        }
        return "DEFAULT_GROUP";
    }

    private void publishChangeEvent(ConfigChangeEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * Utility method for building a change event (called by binder implementations).
     */
    public static ConfigChangeEvent buildChangeEvent(
            Object source,
            String dataId,
            String group,
            String namespace,
            String oldContent,
            String newContent) {
        ConfigChangeType changeType;
        if (oldContent == null) {
            changeType = ConfigChangeType.ADDED;
        } else if (newContent == null) {
            changeType = ConfigChangeType.DELETED;
        } else {
            changeType = ConfigChangeType.MODIFIED;
        }
        return new ConfigChangeEvent(source, dataId, group, namespace, oldContent, newContent, changeType);
    }
}
