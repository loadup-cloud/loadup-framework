package io.github.loadup.commons.enums;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
