package com.github.loadup.components.extension.core;

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

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BizScenario {

  public static final String DEFAULT_BIZ_CODE = "#defaultBizCode#";
  public static final String DEFAULT_USE_CASE = "#defaultUseCase#";
  public static final String DEFAULT_SCENARIO = "#defaultScenario#";
  private static final String DOT_SEPARATOR = ".";

  @Builder.Default String bizCode = DEFAULT_BIZ_CODE;

  @Builder.Default String useCase = DEFAULT_USE_CASE;

  @Builder.Default String scenario = DEFAULT_SCENARIO;

  public String getUniqueIdentity() {
    return bizCode + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
  }

  /** 创建一个只包含 bizCode 的基础场景 */
  public static BizScenario valueOf(String bizCode) {
    return BizScenario.builder().bizCode(bizCode).build();
  }

  /** 创建包含 bizCode 和 useCase 的场景 */
  public static BizScenario valueOf(String bizCode, String useCase) {
    return BizScenario.builder().bizCode(bizCode).useCase(useCase).build();
  }

  /** 创建包含所有维度的完整场景 */
  public static BizScenario valueOf(String bizCode, String useCase, String scenario) {
    return BizScenario.builder().bizCode(bizCode).useCase(useCase).scenario(scenario).build();
  }
}
