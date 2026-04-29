package io.github.loadup.components.configcenter.local.binder;

import io.github.loadup.components.configcenter.binding.impl.AbstractConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.local.cfg.LocalConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 本地文件系统配置 Binder。
 *
 * <p>读取策略（优先级由高到低）：
 * <ol>
 *   <li>本地文件：{@code {basePath}/{group}/{dataId}}</li>
 *   <li>Spring {@code Environment}：{@code environment.getProperty(dataId)}</li>
 * </ol>
 *
 * <p>文件变更通过定时轮询（{@code refreshInterval}）检测，检测到 mtime 变化后回调所有注册的监听器。
 */
@Slf4j
@Component
public class LocalConfigCenterBinder
        extends AbstractConfigCenterBinder<LocalConfigCenterBinderCfg, ConfigCenterBindingCfg> {

    private Environment environment;

    /** dataId+group → list of listeners */
    private final Map<String, List<ListenerEntry>> listenerRegistry = new ConcurrentHashMap<>();

    /** dataId+group → last known mtime */
    private final Map<String, Long> mtimeRegistry = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getBinderType() {
        return "local";
    }

    @Override
    protected void afterConfigInjected(
            String name, LocalConfigCenterBinderCfg cfg, ConfigCenterBindingCfg bindingCfg) {
        long intervalMs = cfg.getRefreshInterval().toMillis();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "configcenter-local-poller");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::pollChanges, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("[ConfigCenter-Local] started with basePath={}, refreshInterval={}ms",
                cfg.getBasePath(), intervalMs);
    }

    @Override
    protected void afterDestroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    // ── Binder API ──────────────────────────────────────────────────────────

    @Override
    public Optional<String> getConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        // 1. 尝试本地文件
        Path filePath = resolvePath(dataId, group);
        if (Files.exists(filePath)) {
            try {
                return Optional.of(Files.readString(filePath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.warn("[ConfigCenter-Local] failed to read file {}: {}", filePath, e.getMessage());
            }
        }
        // 2. fallback: Spring Environment
        String envValue = environment.getProperty(dataId);
        return Optional.ofNullable(envValue);
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content, ConfigCenterBindingCfg settings) {
        Path filePath = resolvePath(dataId, group);
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.debug("[ConfigCenter-Local] published: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("[ConfigCenter-Local] failed to publish {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeConfig(String dataId, String group, ConfigCenterBindingCfg settings) {
        Path filePath = resolvePath(dataId, group);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("[ConfigCenter-Local] removed: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("[ConfigCenter-Local] failed to remove {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    @Override
    public void addListener(
            String dataId, String group, ConfigChangeListener listener, ConfigCenterBindingCfg settings) {
        String key = listenerKey(dataId, group);
        listenerRegistry.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                .add(new ListenerEntry(dataId, group, listener));
        // 记录当前 mtime
        Path filePath = resolvePath(dataId, group);
        if (Files.exists(filePath)) {
            mtimeRegistry.put(key, filePath.toFile().lastModified());
        }
    }

    @Override
    public void removeListener(
            String dataId, String group, ConfigChangeListener listener, ConfigCenterBindingCfg settings) {
        String key = listenerKey(dataId, group);
        List<ListenerEntry> entries = listenerRegistry.get(key);
        if (entries != null) {
            entries.removeIf(e -> e.listener == listener);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Path resolvePath(String dataId, String group) {
        String basePath = binderCfg != null ? binderCfg.getBasePath() : "config";
        String resolvedGroup = (group != null && !group.isBlank()) ? group : "DEFAULT_GROUP";
        return Paths.get(basePath, resolvedGroup, dataId);
    }

    private String listenerKey(String dataId, String group) {
        return group + "/" + dataId;
    }

    private void pollChanges() {
        for (Map.Entry<String, List<ListenerEntry>> entry : listenerRegistry.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            ListenerEntry first = entry.getValue().getFirst();
            Path filePath = resolvePath(first.dataId, first.group);
            File file = filePath.toFile();

            long currentMtime = file.exists() ? file.lastModified() : -1L;
            Long prevMtime = mtimeRegistry.get(entry.getKey());

            if (!Objects.equals(prevMtime, currentMtime)) {
                String newContent = null;
                if (file.exists()) {
                    try {
                        newContent = Files.readString(filePath, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        log.warn("[ConfigCenter-Local] poll read failed: {}", e.getMessage());
                    }
                }
                String oldContent = null;
                ConfigChangeEvent event = DefaultConfigCenterBinding.buildChangeEvent(
                        this, first.dataId, first.group,
                        binderCfg != null ? binderCfg.getNamespace() : "",
                        oldContent, newContent);
                for (ListenerEntry listenerEntry : entry.getValue()) {
                    try {
                        listenerEntry.listener.onChange(event);
                    } catch (Exception e) {
                        log.error("[ConfigCenter-Local] listener error: {}", e.getMessage(), e);
                    }
                }
                mtimeRegistry.put(entry.getKey(), currentMtime);
            }
        }
    }

    private record ListenerEntry(String dataId, String group, ConfigChangeListener listener) {}
}
