package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum LoggerLevelEnum implements IEnum {
    /**
     * DEBUG
     */
    DEBUG("DEBUG", "DEBUG"),

    /**
     * INFO
     */
    INFO("INFO", "INFO"),

    /**
     * WARN
     */
    WARN("WARN", "WARN"),

    /**
     * ERROR
     */
    ERROR("ERROR", "ERROR"),
    ;

    private final String code;

    private final String description;

    public static LoggerLevelEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(LoggerLevelEnum.class, code);
    }

    private LoggerLevelEnum(String code, String description) {
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
