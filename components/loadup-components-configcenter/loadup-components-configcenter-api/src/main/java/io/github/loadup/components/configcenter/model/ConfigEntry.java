package io.github.loadup.components.configcenter.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置条目包装器，封装从配置中心读取的一条完整配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntry {

    /** 配置项 ID（Key）。 */
    private String dataId;

    /** 配置分组。 */
    private String group;

    /** 命名空间 / 租户。 */
    private String namespace;

    /** 配置内容（明文）。 */
    private String content;

    /** 内容类型，如 yaml / properties / json / text。 */
    private String contentType;

    /** 版本号（由配置中心返回，不支持时为 null）。 */
    private String version;

    /** 最后修改时间（由配置中心返回，不支持时为 null）。 */
    private Instant lastModified;
}
