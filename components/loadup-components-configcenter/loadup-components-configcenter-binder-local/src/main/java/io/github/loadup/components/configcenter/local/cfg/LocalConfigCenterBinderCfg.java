package io.github.loadup.components.configcenter.local.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Local binder 配置。
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     binders:
 *       local:
 *         base-path: config/        # 本地配置文件根目录（相对或绝对路径）
 *         refresh-interval: 30s     # 文件变更轮询间隔
 * </pre>
 */
@Getter
@Setter
public class LocalConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /**
     * 本地配置文件根目录。
     * 文件路径规则：{basePath}/{group}/{dataId}
     * 不存在时 fallback 到 Spring {@code Environment.getProperty(dataId)}。
     */
    private String basePath = "config";

    /**
     * 文件变更轮询间隔（用于触发 {@link io.github.loadup.components.configcenter.model.ConfigChangeListener}）。
     */
    private Duration refreshInterval = Duration.ofSeconds(30);

    @Override
    public Object getIdentity() {
        return basePath;
    }
}
