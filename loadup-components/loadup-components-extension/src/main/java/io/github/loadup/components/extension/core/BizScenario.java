package io.github.loadup.components.extension.core;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
