package io.github.loadup.components.configcenter.apollo.binder;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import io.github.loadup.components.configcenter.apollo.cfg.ApolloConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.binding.impl.AbstractConfigCenterBinder;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import io.github.loadup.components.configcenter.model.ConfigChangeType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Ctrip Apollo config-center binder.
 *
 * <p>Reads configuration via Apollo's Java {@link ConfigService} API.
 * Write operations ({@code publishConfig} / {@code removeConfig}) require the Apollo Open API
 * (HTTP) and are currently stubbed with a WARN log pending integration of the Apollo Portal API.
 *
 * <p>Change listening is implemented via {@link Config#addChangeListener(ConfigChangeListener)}.
 */
@Slf4j
@Component
public class ApolloConfigCenterBinder
        extends AbstractConfigCenterBinder<ApolloConfigCenterBinderCfg, ConfigCenterBindingCfg> {

    private Config apolloConfig;

    /** listener key → Apollo ConfigChangeListener (stored for removeListener). */
    private final Map<String, ConfigChangeListener> apolloListenerMap = new ConcurrentHashMap<>();

    @Override
    public String getBinderType() {
        return "apollo";
    }

    @Override
    protected void afterConfigInjected(
            String name, ApolloConfigCenterBinderCfg cfg, ConfigCenterBindingCfg bindingCfg) {
        // Set system properties read by Apollo SDK on startup
        if (StringUtils.hasText(cfg.getMeta())) {
            System.setProperty("apollo.meta", cfg.getMeta());
        }
        if (StringUtils.hasText(cfg.getAppId())) {
            System.setProperty("app.id", cfg.getAppId());
        }
        if (StringUtils.hasText(cfg.getEnv())) {
            System.setProperty("env", cfg.getEnv());
        }
        if (StringUtils.hasText(cfg.getCluster())) {
            System.setProperty("apollo.cluster", cfg.getCluster());
        }

        apolloConfig = ConfigService.getConfig(cfg.getApolloNamespace());
        log.info("[ConfigCenter-Apollo] connected: appId={}, env={}, namespace={}",
                cfg.getAppId(), cfg.getEnv(), cfg.getApolloNamespace());
    }

    @Override
    public Optional<String> getConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        // In Apollo, group maps to namespace; dataId is the property key
        Config cfg = resolveConfig(group);
        String value = cfg.getProperty(dataId, null);
        return Optional.ofNullable(value);
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content, ConfigCenterBindingCfg settings) {
        // TODO: implement via Apollo Open API (HTTP PUT /openapi/v1/envs/.../items/...)
        log.warn("[ConfigCenter-Apollo] publishConfig is not yet implemented via Open API: dataId={}", dataId);
        return false;
    }

    @Override
    public boolean removeConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        // TODO: remove config item via Apollo Open API
        log.warn("[ConfigCenter-Apollo] removeConfig is not yet implemented via Open API: dataId={}", dataId);
        return false;
    }

    @Override
    public void addListener(
            String dataId,
            String group,
            io.github.loadup.components.configcenter.model.ConfigChangeListener listener,
            ConfigCenterBindingCfg settings) {

        Config cfg = resolveConfig(group);
        String listenerKey = listenerKey(dataId, group, listener);

        ConfigChangeListener apolloListener = changeEvent -> {
            if (!changeEvent.changedKeys().contains(dataId)) {
                return;
            }
            ConfigChange change = changeEvent.getChange(dataId);
            ConfigChangeType changeType = switch (change.getChangeType()) {
                case ADDED -> ConfigChangeType.ADDED;
                case MODIFIED -> ConfigChangeType.MODIFIED;
                case DELETED -> ConfigChangeType.DELETED;
            };
            ConfigChangeEvent event = new ConfigChangeEvent(
                    ApolloConfigCenterBinder.this,
                    dataId,
                    group,
                    binderCfg.getNamespace(),
                    change.getOldValue(),
                    change.getNewValue(),
                    changeType);
            listener.onChange(event);
        };

        apolloListenerMap.put(listenerKey, apolloListener);
        cfg.addChangeListener(apolloListener, Set.of(dataId));
    }

    @Override
    public void removeListener(
            String dataId,
            String group,
            io.github.loadup.components.configcenter.model.ConfigChangeListener listener,
            ConfigCenterBindingCfg settings) {

        String listenerKey = listenerKey(dataId, group, listener);
        ConfigChangeListener apolloListener = apolloListenerMap.remove(listenerKey);
        if (apolloListener != null) {
            resolveConfig(group).removeChangeListener(apolloListener);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * If group is non-empty and differs from the default group, treat it as an Apollo namespace
     * and load it; otherwise use the namespace already loaded in afterConfigInjected.
     */
    private Config resolveConfig(String group) {
        if (StringUtils.hasText(group) && !group.equals(binderCfg.getDefaultGroup())) {
            try {
                return ConfigService.getConfig(group);
            } catch (Exception e) {
                log.warn("[ConfigCenter-Apollo] could not load namespace {}: {}", group, e.getMessage());
            }
        }
        return apolloConfig;
    }

    private String listenerKey(
            String dataId,
            String group,
            io.github.loadup.components.configcenter.model.ConfigChangeListener listener) {
        return group + "/" + dataId + "@" + System.identityHashCode(listener);
    }
}
