package io.github.loadup.components.configcenter.nacos.binder;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.loadup.components.configcenter.binding.impl.AbstractConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.components.configcenter.nacos.cfg.NacosConfigCenterBinderCfg;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Nacos config-center binder.
 *
 * <p>Wraps the Nacos {@link ConfigService} to implement config read, publish,
 * removal, and change listening.
 */
@Slf4j
@Component
public class NacosConfigCenterBinder
        extends AbstractConfigCenterBinder<NacosConfigCenterBinderCfg, ConfigCenterBindingCfg> {

    private ConfigService configService;

    /** listener key → nacos AbstractListener (stored for later removeListener calls). */
    private final Map<String, com.alibaba.nacos.api.config.listener.Listener> nacosListenerMap =
            new ConcurrentHashMap<>();

    @Override
    public String getBinderType() {
        return "nacos";
    }

    @Override
    protected void afterConfigInjected(
            String name, NacosConfigCenterBinderCfg cfg, ConfigCenterBindingCfg bindingCfg) {
        try {
            Properties props = buildNacosProperties(cfg);
            configService = NacosFactory.createConfigService(props);
            log.info("[ConfigCenter-Nacos] connected to {} (namespace={})", cfg.getServerAddr(), cfg.getNamespace());
        } catch (NacosException e) {
            throw new IllegalStateException("[ConfigCenter-Nacos] failed to create ConfigService: " + e.getMessage(), e);
        }
    }

    @Override
    protected void afterDestroy() {
        if (configService != null) {
            try {
                configService.shutDown();
            } catch (NacosException e) {
                log.warn("[ConfigCenter-Nacos] error shutting down: {}", e.getMessage());
            }
        }
    }

    // ── Binder API ──────────────────────────────────────────────────────────

    @Override
    public Optional<String> getConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        try {
            String content = configService.getConfig(dataId, resolveGroup(group), binderCfg.getTimeout());
            return Optional.ofNullable(content);
        } catch (NacosException e) {
            log.error("[ConfigCenter-Nacos] getConfig failed: dataId={}, group={}: {}", dataId, group, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content, ConfigCenterBindingCfg settings) {
        try {
            return configService.publishConfig(dataId, resolveGroup(group), content);
        } catch (NacosException e) {
            log.error("[ConfigCenter-Nacos] publishConfig failed: dataId={}: {}", dataId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        try {
            return configService.removeConfig(dataId, resolveGroup(group));
        } catch (NacosException e) {
            log.error("[ConfigCenter-Nacos] removeConfig failed: dataId={}: {}", dataId, e.getMessage());
            return false;
        }
    }

    @Override
    public void addListener(
            String dataId, String group, ConfigChangeListener listener, ConfigCenterBindingCfg settings) {
        String resolvedGroup = resolveGroup(group);
        String listenerKey = listenerKey(dataId, resolvedGroup, listener);

        AbstractListener nacosListener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                ConfigChangeEvent event = DefaultConfigCenterBinding.buildChangeEvent(
                        NacosConfigCenterBinder.this,
                        dataId,
                        resolvedGroup,
                        binderCfg.getNamespace(),
                        null,
                        configInfo);
                listener.onChange(event);
            }
        };

        nacosListenerMap.put(listenerKey, nacosListener);

        try {
            configService.addListener(dataId, resolvedGroup, nacosListener);
        } catch (NacosException e) {
            log.error("[ConfigCenter-Nacos] addListener failed: dataId={}: {}", dataId, e.getMessage());
        }
    }

    @Override
    public void removeListener(
            String dataId, String group, ConfigChangeListener listener, ConfigCenterBindingCfg settings) {
        String resolvedGroup = resolveGroup(group);
        String listenerKey = listenerKey(dataId, resolvedGroup, listener);
        com.alibaba.nacos.api.config.listener.Listener nacosListener = nacosListenerMap.remove(listenerKey);
        if (nacosListener != null) {
            configService.removeListener(dataId, resolvedGroup, nacosListener);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Properties buildNacosProperties(NacosConfigCenterBinderCfg cfg) {
        Properties props = new Properties();
        props.setProperty("serverAddr", cfg.getServerAddr());
        if (StringUtils.hasText(cfg.getNamespace())) {
            props.setProperty("namespace", cfg.getNamespace());
        }
        if (StringUtils.hasText(cfg.getUsername())) {
            props.setProperty("username", cfg.getUsername());
        }
        if (StringUtils.hasText(cfg.getPassword())) {
            props.setProperty("password", cfg.getPassword());
        }
        if (StringUtils.hasText(cfg.getAccessToken())) {
            props.setProperty("accessToken", cfg.getAccessToken());
        }
        return props;
    }

    private String resolveGroup(String group) {
        if (StringUtils.hasText(group)) {
            return group;
        }
        return binderCfg != null && StringUtils.hasText(binderCfg.getDefaultGroup())
                ? binderCfg.getDefaultGroup()
                : "DEFAULT_GROUP";
    }

    private String listenerKey(String dataId, String group, ConfigChangeListener listener) {
        return group + "/" + dataId + "@" + System.identityHashCode(listener);
    }
}
