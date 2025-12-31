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

import com.github.loadup.components.captcha.engine.Symbol;
import com.googlecode.aviator.AviatorEvaluator;
import java.util.ArrayList;
import java.util.List;

/** 算术验证码抽象类 Created by 王帆 on 2019-08-23 上午 10:08. */
public abstract class ArithmeticCaptchaAbstract extends Captcha {

  /** 计算公式 */
  private String arithmeticString;

  /** 难度 */
  protected static int difficulty = 10;

  /** 表达式复杂度 */
  protected static int algorithmSign = 4;

  public ArithmeticCaptchaAbstract() {
    setLen(2);
  }

  /**
   * 生成随机验证码
   *
   * @return 验证码字符数组
   */
  @Override
  protected char[] alphas() {
    List<String> arithmeticList = new ArrayList<>(len + len - 1);
    Symbol lastSymbol = null;
    int divAmount = 0;
    for (int i = 0; i < len; i++) {
      int number = num(difficulty);

      // 如果上一步生成的为除号，要重新设置除数和被除数，确保难度满足设定要求且可以整除
      if (lastSymbol == Symbol.DIV) {
        number = (int) Math.sqrt(number);
        // 避免被除数为 0
        number = number == 0 ? 1 : number;
        arithmeticList.set(2 * (i - 1), String.valueOf(number * num((int) Math.sqrt(difficulty))));
      }

      // 如果是减法则获取一个比第一个小的数据
      if (lastSymbol == Symbol.SUB) {
        String firstNum = arithmeticList.get(0);
        number = num(Integer.parseInt(firstNum) + 1);
      }

      arithmeticList.add(String.valueOf(number));

      if (i < len - 1) {
        int type;

        // 除法只出现一次，否则还需要递归更新除数，第一个除数将会很大
        if (divAmount == 1) {
          type = num(1, algorithmSign - 1);
        } else {
          type = num(1, algorithmSign);
        }

        if (type == 1) {
          arithmeticList.add((lastSymbol = Symbol.ADD).getValue());
        } else if (type == 2) {
          arithmeticList.add((lastSymbol = Symbol.SUB).getValue());
        } else if (type == 3) {
          arithmeticList.add((lastSymbol = Symbol.MUL).getValue());
        } else if (type == 4) {
          arithmeticList.add((lastSymbol = Symbol.DIV).getValue());
          divAmount++;
        }
      }
    }
    arithmeticString = String.join("", arithmeticList);
    chars =
        String.valueOf(
            AviatorEvaluator.execute(arithmeticString.replace("x", "*").replace("÷", "/")));
    arithmeticString += "=?";
    return chars.toCharArray();
  }

  public String getArithmeticString() {
    checkAlpha();
    return arithmeticString;
  }

  public void setArithmeticString(String arithmeticString) {
    this.arithmeticString = arithmeticString;
  }

  public void setDifficulty(int difficulty) {
    // 做上下界检测，避免越界
    if (difficulty <= 0) {
      difficulty = 10;
    }
    ArithmeticCaptchaAbstract.difficulty = difficulty;
  }

  /**
   * algorithmSign
   *
   * <p>2 : 支持加法 algorithmSign
   *
   * <p>3 : 支持加减法 algorithmSign
   *
   * <p>4 : 支持加减乘法
   *
   * <p>5 : 支持加减乘除法
   *
   * @param algorithmSign 计算公式标示
   */
  public void supportAlgorithmSign(int algorithmSign) {

    // 做上下界检测，避免越界
    if (algorithmSign < 2) {
      algorithmSign = 2;
    }

    if (algorithmSign > 5) {
      algorithmSign = 5;
    }
    ArithmeticCaptchaAbstract.algorithmSign = algorithmSign;
  }
}
