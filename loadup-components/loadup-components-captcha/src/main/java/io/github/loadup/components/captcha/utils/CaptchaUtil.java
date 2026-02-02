package io.github.loadup.components.captcha.utils;

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

import io.github.loadup.components.captcha.SpecCaptcha;
import io.github.loadup.components.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;

/**
 * 图形验证码工具类
 *
 * @author 王帆
 * @since 2018-07-27 上午 10:08
 */
public class CaptchaUtil extends AbstractCaptchaUtil {

  /**
   * 输出验证码
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    out(DEFAULT_LEN, request, response);
  }

  /**
   * 输出验证码
   *
   * @param len 长度
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(int len, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    out(DEFAULT_WIDTH, DEFAULT_HEIGHT, len, request, response);
  }

  /**
   * 输出验证码
   *
   * @param width 宽度
   * @param height 高度
   * @param len 长度
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(
      int width, int height, int len, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    out(width, height, len, null, 0, request, response);
  }

  /**
   * 输出验证码
   *
   * @param font 字体
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(Font font, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    out(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_LEN, font, 0, request, response);
  }

  /**
   * 输出验证码
   *
   * @param len 长度
   * @param font 字体
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(
      int len, Font font, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    out(DEFAULT_WIDTH, DEFAULT_HEIGHT, len, font, 0, request, response);
  }

  /**
   * 输出验证码
   *
   * @param width 宽度
   * @param height 高度
   * @param len 长度
   * @param font 字体
   * @param type 验证码字符类型
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(
      int width,
      int height,
      int len,
      Font font,
      int type,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    SpecCaptcha specCaptcha = new SpecCaptcha(width, height, len);
    if (font != null) {
      specCaptcha.setFont(font);
    }
    if (type > 0 && type <= 6) {
      specCaptcha.setCharType(type);
    }
    out(specCaptcha, request, response);
  }

  /**
   * 输出验证码
   *
   * @param width 宽度
   * @param height 高度
   * @param len 长度
   * @param type 验证码字符类型
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(
      int width,
      int height,
      int len,
      int type,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    out(width, height, len, null, type, request, response);
  }

  /**
   * 输出验证码
   *
   * @param captcha Captcha
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException IO异常
   */
  public static void out(Captcha captcha, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    setHeader(response);
    request.getSession().setAttribute(SESSION_KEY, captcha.text().toLowerCase());
    captcha.out(response.getOutputStream());
  }

  /**
   * 验证验证码
   *
   * @param code 用户输入的验证码
   * @param request HttpServletRequest
   * @return 是否正确
   */
  public static boolean ver(String code, HttpServletRequest request) {
    if (code != null) {
      String captcha = (String) request.getSession().getAttribute(SESSION_KEY);
      return code.trim().toLowerCase().equals(captcha);
    }
    return false;
  }

  /**
   * 清除session中的验证码
   *
   * @param request HttpServletRequest
   */
  public static void clear(HttpServletRequest request) {
    request.getSession().removeAttribute(SESSION_KEY);
  }

  /**
   * 设置相应头
   *
   * @param response HttpServletResponse
   */
  public static void setHeader(HttpServletResponse response) {
    response.setContentType("image/gif");
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
  }
}
