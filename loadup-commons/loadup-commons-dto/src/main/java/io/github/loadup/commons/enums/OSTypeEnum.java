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
