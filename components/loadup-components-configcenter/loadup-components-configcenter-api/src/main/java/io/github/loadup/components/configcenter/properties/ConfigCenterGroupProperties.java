package io.github.loadup.components.configcenter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置中心全局属性。
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     default-binder: local    # local | nacos | apollo
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.configcenter")
public class ConfigCenterGroupProperties {

    /** 默认 binder 类型，未在 binding 级别指定时使用。 */
    private ConfigCenterBinderType defaultBinder = ConfigCenterBinderType.LOCAL;
}
