package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum TimeUnitEnum implements IEnum {
    /**
     * SECOND
     */
    SECOND("S", "SECOND"),
    /**
     * MINUTE
     */
    MINUTE("I", "MINUTE"),
    /**
     * HOUR
     */
    HOUR("H", "HOUR"),
    /**
     * DAY
     */
    DAY("D", "DAY"),
    /**
     * MONTH
     */
    MONTH("D", "MONTH"),
    /**
     * YEAR
     */
    YEAR("Y", "YEAR"),
    ;
    private String code;
    private String description;

    public static TimeUnitEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(TimeUnitEnum.class, code);
    }

    private TimeUnitEnum(String code, String description) {
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
