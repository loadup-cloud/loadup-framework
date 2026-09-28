package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum OSTypeEnum implements IEnum {
    /**
     * Android
     */
    ANDROID("Android", "Android"),

    /**
     * IOS
     */
    IOS("IOS", "IOS"),

    /**
     * Windows
     */
    WINDOWS("WINDOWS", "Windows"),

    /**
     * MACOS
     */
    MACOS("MACOS", "MacOS"),

    /**
     * iPadOS
     */
    IPADOS("IPADOS", "iPadOS"),
    /**
     * HarmonyOS
     */
    HARMONY_OS("HARMONY_OS", "HarmonyOS"),

    /**
     * Linux
     */
    LINUX("LINUX", "Linux"),
    ;

    /**
     * 终端类型代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    public static OSTypeEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(OSTypeEnum.class, code);
    }

    private OSTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
