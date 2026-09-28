package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum TerminalTypeEnum implements IEnum {
    /**
     * APP
     */
    APP("APP", "APP"),

    /**
     * WEB
     */
    WEB("WEB", "WEB"),

    /**
     * WAP
     */
    WAP("WAP", "WAP"),

    /**
     * SYSTEM
     */
    SYSTEM("SYSTEM", "SYSTEM"),

    /**
     * PC
     */
    PC("PC", "PC"),
    ;

    private final String code;

    private final String description;

    public static TerminalTypeEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(TerminalTypeEnum.class, code);
    }

    private TerminalTypeEnum(String code, String description) {
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
