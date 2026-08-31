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
