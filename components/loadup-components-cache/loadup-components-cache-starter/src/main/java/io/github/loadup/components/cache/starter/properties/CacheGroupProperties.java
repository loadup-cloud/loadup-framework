
package io.github.loadup.components.cache.starter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lise
 * @version CacheGroupProperties.java, v 0.1 2026年01月13日 16:38 lise
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.cache")
public class CacheGroupProperties {
    /** 绑定的存储类型 */
    private CacheBinderType defaultBinder = CacheBinderType.CAFFEINE;

}
