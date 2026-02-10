// package io.github.loadup.testify.starter.testng;

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
//
// import com.fasterxml.jackson.databind.JsonNode;
// import model.core.testify.loadup.github.io.TestContext;
// import util.core.testify.loadup.github.io.JsonUtil;
// import loader.starter.testify.loadup.github.io.YamlLoader;
// import java.lang.reflect.Method;
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;
// import org.testng.ITestContext;
// import org.testng.annotations.DataProvider;
//
/// **
// * DataProvider 职责： 1. 加载物理 YAML 文件。
// *
// * <p>2. 绑定 TestContext 到 TestNG 上下文。
// *
// * <p>3. 将原始的 input 节点转换为方法入参（不解析变量）。
// */
// public class TestifyDataProvider {
//
//  private final YamlLoader yamlLoader = new YamlLoader();
//
//  @DataProvider(name = "testifyData")
//  public Iterator<Object[]> provideTestData(ITestContext testngContext, Method method) {
//    List<Object[]> dataList = new ArrayList<>();
//
//    try {
//      // 1. 根据约定路径加载原始 YAML (此时不包含变量解析逻辑)
//      String className = method.getDeclaringClass().getSimpleName();
//      String methodName = method.getName();
//      String yamlPath = String.format("testcases/%s/%s.yaml", className, methodName);
//
//      TestContext testContext = yamlLoader.load(yamlPath);
//      if (testContext == null) {
//        throw new RuntimeException("Cannot find YAML file at: " + yamlPath);
//      }
//
//      // 2. 将 TestContext 绑定到 TestNG 运行上下文中，供 TestifyBase 获取
//      // 使用类名+方法名作为唯一 Key
//      String contextKey = method.getDeclaringClass().getName() + "." + method.getName();
//      testngContext.setAttribute(contextKey, testContext);
//
//      // 3. 将 input 节点映射为方法参数
//      JsonNode inputNode = testContext.input();
//      if (inputNode != null) {
//        // 处理多组数据驱动：[[p1, p2], [p3, p4]]
//        if (inputNode.isArray() && inputNode.size() > 0 && inputNode.get(0).isArray()) {
//          for (JsonNode row : inputNode) {
//            dataList.add(convertToArgs(row, method.getParameterTypes()));
//          }
//        } else {
//          // 处理单组参数：[p1, p2] 或单个对象
//          dataList.add(convertToArgs(inputNode, method.getParameterTypes()));
//        }
//      } else {
//        // 无参数情况
//        dataList.add(new Object[method.getParameterCount()]);
//      }
//
//    } catch (Exception e) {
//      throw new RuntimeException(
//          "Testify DataProvider failed for " + method.getName() + ": " + e.getMessage(), e);
//    }
//
//    return dataList.iterator();
//  }
//
//  /** 将 JsonNode 转换为 Java 方法参数。 注意：此时参数值可能包含未解析的占位符字符串，如 "${faker.name()}"。 */
//  private static Object[] convertToArgs(JsonNode inputNode, Class<?>[] parameterTypes) {
//      int paramCount = parameterTypes.length;
//      Object[] args = new Object[paramCount];
//
//      if (inputNode == null || inputNode.isNull()) {
//          return args;
//      }
//
//      // 场景 A: YAML input 是一个数组 [arg1, arg2, ...]
//      if (inputNode.isArray()) {
//          for (int i = 0; i < paramCount; i++) {
//              if (i < inputNode.size()) {
//                  // 利用 JsonUtil 将 JsonNode (可能是对象、字符串或数字) 转换为对应的 parameterTypes[i]
//                  // 注意：如果目标是 POJO，此时字段里的 ${var} 依然在字符串里
//                  args[i] = JsonUtil.convertValue(inputNode.get(i), parameterTypes[i]);
//              } else {
//                  args[i] = null;
//              }
//          }
//      }
//      // 场景 B: YAML input 是单个对象或基本类型 {id:1...} 或 "string"
//      // 且测试方法只有一个参数
//      else if (paramCount == 1) {
//          args[0] = JsonUtil.convertValue(inputNode, parameterTypes[0]);
//      }
//      // 场景 C: YAML 传了单个非数组值，但方法有多个参数（通常是配置错误）
//      else {
//          // 尝试把 inputNode 填充给第一个参数，其余置空
//          args[0] = JsonUtil.convertValue(inputNode, parameterTypes[0]);
//      }
//
//      return args;
//  }
// }
