package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum GenderEnum implements IEnum {
    MALE("MALE", "Male"),
    FEMALE("FEMALE", "Female"),
    UNKNOWN("UNKNOWN", "Unknown"),
    ;

    private final String code;
    private final String description;

    public static GenderEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(GenderEnum.class, code);
    }

    private GenderEnum(String code, String description) {
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
