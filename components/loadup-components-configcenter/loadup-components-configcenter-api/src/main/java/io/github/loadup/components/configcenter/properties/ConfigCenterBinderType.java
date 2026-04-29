package io.github.loadup.components.configcenter.properties;

/**
 * 配置中心 Binder 类型枚举。
 */
public enum ConfigCenterBinderType {

    /** 本地文件系统 + Spring Environment fallback。 */
    LOCAL("local"),

    /** Alibaba Nacos 配置中心。 */
    NACOS("nacos"),

    /** Ctrip Apollo 配置中心。 */
    APOLLO("apollo");

    private final String value;

    ConfigCenterBinderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
