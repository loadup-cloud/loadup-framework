package io.github.loadup.components.configcenter.properties;

/**
 * Enum of supported config-center binder types.
 */
public enum ConfigCenterBinderType {

    /** Local file system with Spring Environment fallback. */
    LOCAL("local"),

    /** Alibaba Nacos config center. */
    NACOS("nacos"),

    /** Ctrip Apollo config center. */
    APOLLO("apollo");

    private final String value;

    ConfigCenterBinderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
