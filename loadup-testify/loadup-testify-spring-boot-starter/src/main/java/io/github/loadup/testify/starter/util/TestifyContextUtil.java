package io.github.loadup.testify.starter.util;

/*-
 * #%L
 * Testify Spring Boot Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.lang.reflect.Method;
import org.testng.ITestResult;

public class TestifyContextUtil {
  public static String getContextKey(Method method) {
    return method.getDeclaringClass().getName() + "." + method.getName();
  }

  // 在 Listener 中对应的获取方式
  public static String getContextKey(ITestResult result) {
    return result.getMethod().getQualifiedName();
    // 注意：TestNG 的 getQualifiedName() 默认就是 "package.class.method"
  }
}
