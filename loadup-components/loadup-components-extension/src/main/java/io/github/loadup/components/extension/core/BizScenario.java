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

    public static final String DEFAULT_BIZ_CODE = "#defaultBizCode#";
    public static final String DEFAULT_USE_CASE = "#defaultUseCase#";
    public static final String DEFAULT_SCENARIO = "#defaultScenario#";
    private static final String DOT_SEPARATOR = ".";

    public BizScenario {
        if (bizCode == null) bizCode = DEFAULT_BIZ_CODE;
        if (useCase == null) useCase = DEFAULT_USE_CASE;
        if (scenario == null) scenario = DEFAULT_SCENARIO;
    }

    public String getUniqueIdentity() {
        return bizCode + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
    }

    public static BizScenario valueOf(String bizCode) {
        return BizScenario.builder().bizCode(bizCode).build();
    }

    public static BizScenario valueOf(String bizCode, String useCase) {
        return BizScenario.builder().bizCode(bizCode).useCase(useCase).build();
    }

    public static BizScenario valueOf(String bizCode, String useCase, String scenario) {
        return BizScenario.builder()
                .bizCode(bizCode)
                .useCase(useCase)
                .scenario(scenario)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String bizCode = DEFAULT_BIZ_CODE;
        private String useCase = DEFAULT_USE_CASE;
        private String scenario = DEFAULT_SCENARIO;

        public Builder bizCode(String bizCode) {
            this.bizCode = bizCode;
            return this;
        }

        public Builder useCase(String useCase) {
            this.useCase = useCase;
            return this;
        }

        public Builder scenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        public BizScenario build() {
            return new BizScenario(bizCode, useCase, scenario);
        }
    }
}
