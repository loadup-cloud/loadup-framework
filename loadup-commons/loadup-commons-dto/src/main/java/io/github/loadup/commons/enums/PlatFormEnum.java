package io.github.loadup.commons.enums;

/**
 * @author Lise
 * @since 1.0.0
 */
public enum PlatFormEnum implements IEnum {
    /**
     * X86
     */
    X86("X86", "X86"),
    /**
     * X86_64
     */
    X86_64("X86_64", "X86_64"),
    /**
     * ARM64
     */
    ARM64("ARM64", "ARM64"),
    /**
     * SPARC
     */
    SPARC("SPARC", "SPARC"),
    /**
     * MIPS
     */
    MIPS("MIPS", "MIPS"),
    ;
    private String code;
    private String description;

    public static PlatFormEnum getByCode(String code) {
        return IEnum.EnumLookup.fromCode(PlatFormEnum.class, code);
    }

    private PlatFormEnum(String code, String description) {
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
