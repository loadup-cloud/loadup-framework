package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-lang
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

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Lise
 * @since 1.0.0
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

  private static final char BACKSLASH = '\\';

  /**
   * 格式化字符串<br>
   * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
   * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
   * 例：<br>
   * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b<br>
   * 转义{}： format("this is \\{} for {}", "a", "b") =》 this is \{} for a<br>
   * 转义\： format("this is \\\\{} for {}", "a", "b") =》 this is \a for b<br>
   */
  public static String format(String strPattern, Object... argArray) {
    return formatWith(strPattern, "{}", argArray);
  }

  /**
   * 格式化字符串<br>
   * 此方法只是简单将指定占位符 按照顺序替换为参数<br>
   * 如果想输出占位符使用 \\转义即可，如果想输出占位符之前的 \ 使用双转义符 \\\\ 即可<br>
   * 例：<br>
   * 通常使用：format("this is {} for {}", "{}", "a", "b") =》 this is a for b<br>
   * 转义{}： format("this is \\{} for {}", "{}", "a", "b") =》 this is {} for a<br>
   * 转义\： format("this is \\\\{} for {}", "{}", "a", "b") =》 this is \a for b<br>
   *
   * @since 5.7.14
   */
  private static String formatWith(String strPattern, String placeHolder, Object... argArray) {
    if (isBlank(strPattern) || isBlank(placeHolder) || ArrayUtils.isEmpty(argArray)) {
      return strPattern;
    }
    final int strPatternLength = strPattern.length();
    final int placeHolderLength = placeHolder.length();

    // 初始化定义好的长度以获得更好的性能
    final StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

    int handledPosition = 0; // 记录已经处理到的位置
    int delimIndex; // 占位符所在位置
    for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
      delimIndex = strPattern.indexOf(placeHolder, handledPosition);
      if (delimIndex == -1) { // 剩余部分无占位符
        if (handledPosition == 0) { // 不带占位符的模板直接返回
          return strPattern;
        }
        // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
        sbuf.append(strPattern, handledPosition, strPatternLength);
        return sbuf.toString();
      }

      // 转义符
      if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == BACKSLASH) { // 转义符
        if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == BACKSLASH) { // 双转义符
          // 转义符之前还有一个转义符，占位符依旧有效
          sbuf.append(strPattern, handledPosition, delimIndex - 1);
          sbuf.append(argArray[argIndex]);
          handledPosition = delimIndex + placeHolderLength;
        } else {
          // 占位符被转义
          argIndex--;
          sbuf.append(strPattern, handledPosition, delimIndex - 1);
          sbuf.append(placeHolder.charAt(0));
          handledPosition = delimIndex + 1;
        }
      } else { // 正常占位符
        sbuf.append(strPattern, handledPosition, delimIndex);
        sbuf.append(argArray[argIndex]);
        handledPosition = delimIndex + placeHolderLength;
      }
    }

    // 加入最后一个占位符后所有的字符
    sbuf.append(strPattern, handledPosition, strPatternLength);

    return sbuf.toString();
  }

  /**
   * 格式化文本，使用 {varName} 占位<br>
   * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
   *
   * @since 5.7.10
   */
  public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
    if (null == template) {
      return null;
    }
    if (null == map || map.isEmpty()) {
      return template.toString();
    }

    String template2 = template.toString();
    String value;
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      value = String.valueOf(entry.getValue());
      if (null == value && ignoreNull) {
        continue;
      }
      template2 = replace(template2, "{" + entry.getKey() + "}", value);
    }
    return template2;
  }

  /**
   * 格式化文本，使用 {varName} 占位<br>
   * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
   */
  public static String format(CharSequence template, Map<?, ?> map) {
    return format(template, map, true);
  }

  /**
   * 有序的格式化文本，使用{number}做为占位符<br>
   * 通常使用：format("this is {0} for {1}", "a", "b") =》 this is a for b<br>
   */
  public static String formatWithIndex(CharSequence pattern, Object... arguments) {
    return MessageFormat.format(pattern.toString(), arguments);
  }

  /** to string ,default json style */
  public static String toString(Object obj) {
    return ToStringUtils.reflectionToString(obj);
  }

  /** to string */
  public static String toString(Object obj, ToStringStyle style) {
    if ((Objects.isNull(style))) {
      return toString(obj);
    }
    return ToStringUtils.reflectionToString(obj, style);
  }
}
