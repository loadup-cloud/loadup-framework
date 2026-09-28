package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum BooleanEnum implements IEnum {

    /**
     * true
     */
    TRUE("Y", "True"),
    /**
     * false
     */
    FALSE("N", "False");

    private final String code;
    private final String description;

    public static BooleanEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(BooleanEnum.class, code);
    }

    private BooleanEnum(String code, String description) {
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
