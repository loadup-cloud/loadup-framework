package com.github.loadup.components.captcha.utils;

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

/**
 * 图形验证码工具抽象父类
 *
 * @author FULaBUla
 * @since 2023-01-28 上午 10:08
 */
public abstract class AbstractCaptchaUtil {

  /** session 键名 */
  protected static final String SESSION_KEY = "captcha";

  /** 默认长度 */
  protected static final int DEFAULT_LEN = 4;

  /** 默认宽度 */
  protected static final int DEFAULT_WIDTH = 130;

  /** 默认高度 */
  protected static final int DEFAULT_HEIGHT = 48;
}
