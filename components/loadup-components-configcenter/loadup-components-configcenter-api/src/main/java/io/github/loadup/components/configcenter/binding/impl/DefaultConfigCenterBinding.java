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
 * {@link ConfigCenterBinding} 默认实现。
 *
 * <p>委托底层 {@link ConfigCenterBinder} 完成实际读写，同时将变更事件发布到 Spring 事件总线，
 * 使 {@code @EnableConfigAutoRefresh} 等机制能够感知变化。
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

    // ── 查询 ────────────────────────────────────────────────────────────────

    @Override
    public Optional<String> getConfig(String dataId) {
        return getConfig(dataId, resolveGroup(null));
    }

    @Override
    public Optional<String> getConfig(String dataId, String group) {
        ConfigCenterBinder binder = getBinder();
        return binder.getConfig(dataId, group, bindingCfg);
    }

    // ── 发布 ────────────────────────────────────────────────────────────────

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

    // ── 删除 ────────────────────────────────────────────────────────────────

    @Override
    public boolean removeConfig(String dataId) {
        return removeConfig(dataId, resolveGroup(null));
    }

    @Override
    public boolean removeConfig(String dataId, String group) {
        ConfigCenterBinder binder = getBinder();
        return binder.removeConfig(dataId, group, bindingCfg);
    }

    // ── 监听 ────────────────────────────────────────────────────────────────

    @Override
    public void addListener(String dataId, ConfigChangeListener listener) {
        addListener(dataId, resolveGroup(null), listener);
    }

    @Override
    public void addListener(String dataId, String group, ConfigChangeListener listener) {
        ConfigCenterBinder binder = getBinder();
        // 包装原始 listener：先回调业务监听器，再发布 Spring 事件
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

    // ── 私有辅助 ─────────────────────────────────────────────────────────────

    private String resolveGroup(String group) {
        if (group != null && !group.isBlank()) {
            return group;
        }
        if (bindingCfg != null && bindingCfg.getGroup() != null && !bindingCfg.getGroup().isBlank()) {
            return bindingCfg.getGroup();
        }
        // fallback：从 binderCfg 获取 defaultGroup
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
     * 工具方法：构建变更事件（供 binder 实现调用）。
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
