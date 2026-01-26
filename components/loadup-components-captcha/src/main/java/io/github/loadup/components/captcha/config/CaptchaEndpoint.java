package io.github.loadup.components.captcha.config;

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

import io.github.loadup.components.captcha.ArithmeticCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lengleng
 * @date 2020/7/31
 */
@Controller
@RequiredArgsConstructor
public class CaptchaEndpoint {

  private final CaptchaProperties properties;

  /**
   * 生成验证码
   *
   * @param response 响应流
   */
  @SneakyThrows
  @GetMapping("${captcha.create.path:/create}")
  public void create(HttpServletResponse response) {
    ArithmeticCaptcha captcha =
        new ArithmeticCaptcha(properties.getWidth(), properties.getHeight());
    String result = captcha.text();
    // 设置响应头
    response.setContentType(captcha.getContentType());
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    // 转换流信息写出
    captcha.out(response.getOutputStream());
  }
}
