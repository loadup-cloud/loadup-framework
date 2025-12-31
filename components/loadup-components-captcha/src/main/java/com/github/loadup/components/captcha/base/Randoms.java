package com.github.loadup.components.captcha.base;

/*-
 * #%L
 * loadup-components-captcha
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

import java.security.SecureRandom;

/** 随机数工具类 Created by 王帆 on 2018-07-27 上午 10:08. 优化：支持自定义字符集，移除容易混淆的字符（0, O, 1, I, L, i, l） */
public class Randoms {

  protected static final SecureRandom RANDOM = new SecureRandom();

  // 默认验证码字符集：已去除容易混淆的字符 0(零), O(大写o), 1(数字一), I(大写i), L(大写l), i(小写i), l(小写l)
  public static final char DEFAULT_ALPHA[] = {
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9', // 数字：去除了 0 和 1
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
    'G',
    'H',
    'J',
    'K',
    'M',
    'N',
    'P',
    'Q',
    'R',
    'S',
    'T',
    'U',
    'V',
    'W',
    'X',
    'Y',
    'Z',
    // 大写字母：去除了 I, L, O
    'a',
    'b',
    'c',
    'd',
    'e',
    'f',
    'g',
    'h',
    'j',
    'k',
    'm',
    'n',
    'p',
    'q',
    'r',
    's',
    't',
    'u',
    'v',
    'w',
    'x',
    'y',
    'z'
    // 小写字母：去除了 i, l, o
  };

  // 当前使用的字符集，可通过 setCustomAlpha() 自定义
  protected static char[] ALPHA = DEFAULT_ALPHA.clone();

  protected static int numMaxIndex = 8; // 数字的最大索引，不包括最大值

  protected static int charMinIndex = numMaxIndex; // 字符的最小索引，包括最小值

  protected static int charMaxIndex = ALPHA.length; // 字符的最大索引，不包括最大值

  protected static int upperMinIndex = charMinIndex; // 大写字符最小索引

  protected static int upperMaxIndex = upperMinIndex + 23; // 大写字符最大索引

  protected static int lowerMinIndex = upperMaxIndex; // 小写字母最小索引

  protected static int lowerMaxIndex = charMaxIndex; // 小写字母最大索引

  /**
   * 设置自定义字符集
   *
   * @param customAlpha 自定义字符数组
   */
  public static void setCustomAlpha(char[] customAlpha) {
    if (customAlpha == null || customAlpha.length == 0) {
      throw new IllegalArgumentException("Custom alpha cannot be null or empty");
    }
    ALPHA = customAlpha.clone();
    updateIndices();
  }

  /**
   * 设置自定义字符集（字符串形式）
   *
   * @param customAlpha 自定义字符串
   */
  public static void setCustomAlpha(String customAlpha) {
    if (customAlpha == null || customAlpha.isEmpty()) {
      throw new IllegalArgumentException("Custom alpha cannot be null or empty");
    }
    setCustomAlpha(customAlpha.toCharArray());
  }

  /** 重置为默认字符集 */
  public static void resetToDefault() {
    ALPHA = DEFAULT_ALPHA.clone();
    updateIndices();
  }

  /**
   * 获取当前字符集
   *
   * @return 当前字符集的副本
   */
  public static char[] getCurrentAlpha() {
    return ALPHA.clone();
  }

  /** 更新索引值（当字符集改变时调用） */
  private static void updateIndices() {
    // 重新计算索引
    // 默认情况：数字8个(2-9)，大写23个，小写23个
    // 自定义时可能需要调整，这里保持原有逻辑
    charMaxIndex = ALPHA.length;
    lowerMaxIndex = charMaxIndex;
  }

  /**
   * 产生两个数之间的随机数
   *
   * @param min 最小值
   * @param max 最大值
   * @return 随机数
   */
  public static int num(int min, int max) {
    return min + RANDOM.nextInt(max - min);
  }

  /**
   * 产生0-num的随机数,不包括num
   *
   * @param num 最大值
   * @return 随机数
   */
  public static int num(int num) {
    return RANDOM.nextInt(num);
  }

  /**
   * 返回ALPHA中的随机字符
   *
   * @return 随机字符
   */
  public static char alpha() {
    return ALPHA[num(ALPHA.length)];
  }

  /**
   * 返回ALPHA中第0位到第num位的随机字符
   *
   * @param num 到第几位结束
   * @return 随机字符
   */
  public static char alpha(int num) {
    return ALPHA[num(num)];
  }

  /**
   * 返回ALPHA中第min位到第max位的随机字符
   *
   * @param min 从第几位开始
   * @param max 到第几位结束
   * @return 随机字符
   */
  public static char alpha(int min, int max) {
    return ALPHA[num(min, max)];
  }
}
