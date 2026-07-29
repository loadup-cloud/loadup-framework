package io.github.loadup.components.extension.core;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

public record BizScenario(String bizCode, String useCase, String scenario) {

    public static final String DEFAULT_USE_CASE = "#defaultUseCase#";
    public static final String DEFAULT_SCENARIO = "#defaultScenario#";
    private static final String DOT_SEPARATOR = ".";

    public String getUniqueIdentity() {
        return bizCode + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
    }

    public static BizScenario valueOf(String bizCode) {
        return new BizScenario(bizCode, DEFAULT_USE_CASE, DEFAULT_SCENARIO);
    }

    public static BizScenario valueOf(String bizCode, String useCase) {
        return new BizScenario(bizCode, useCase, DEFAULT_SCENARIO);
    }

    public static BizScenario valueOf(String bizCode, String useCase, String scenario) {
        return new BizScenario(bizCode, useCase, scenario);
    }
}
