package io.github.loadup.commons.enums;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
