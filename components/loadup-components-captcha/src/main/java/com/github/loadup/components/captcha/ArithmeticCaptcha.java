package com.github.loadup.components.captcha;

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

import com.github.loadup.components.captcha.base.ArithmeticCaptchaAbstract;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/** png格式验证码 Created by 王帆 on 2018-07-27 上午 10:08. */
@Slf4j
public class ArithmeticCaptcha extends ArithmeticCaptchaAbstract {

  public ArithmeticCaptcha() {}

  public ArithmeticCaptcha(int width, int height) {
    this();
    setWidth(width);
    setHeight(height);
  }

  public ArithmeticCaptcha(int width, int height, int len) {
    this(width, height);
    setLen(len);
  }

  public ArithmeticCaptcha(int width, int height, int len, Font font) {
    this(width, height, len);
    setFont(font);
  }

  /**
   * 生成验证码
   *
   * @param out 输出流
   * @return 是否成功
   */
  @Override
  public boolean out(OutputStream out) {
    checkAlpha();
    return graphicsImage(getArithmeticString().toCharArray(), out);
  }

  @Override
  public String toBase64() {
    return toBase64("data:image/png;base64,");
  }

  @Override
  public String getContentType() {
    return "image/png";
  }

  /**
   * 生成验证码图形
   *
   * @param strs 验证码
   * @param out 输出流
   * @return boolean
   */
  private boolean graphicsImage(char[] strs, OutputStream out) {
    BufferedImage bi = null;
    try {
      bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = (Graphics2D) bi.getGraphics();
      // 填充背景
      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, width, height);
      // 抗锯齿
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // 画干扰圆
      drawOval(2, g2d);
      // 画字符串
      g2d.setFont(getFont());
      FontMetrics fontMetrics = g2d.getFontMetrics();
      int fW = width / strs.length; // 每一个字符所占的宽度
      int fSp = (fW - (int) fontMetrics.getStringBounds("8", g2d).getWidth()) / 2; // 字符的左右边距
      for (int i = 0; i < strs.length; i++) {
        g2d.setColor(color());
        int fY =
            height
                - ((height
                        - (int)
                            fontMetrics.getStringBounds(String.valueOf(strs[i]), g2d).getHeight())
                    >> 1); // 文字的纵坐标
        g2d.drawString(String.valueOf(strs[i]), i * fW + fSp + 3, fY - 3);
      }
      g2d.dispose();
      ImageIO.write(bi, "png", out);
      out.flush();
      return true;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }

      if (bi != null) {
        bi.getGraphics().dispose();
      }
    }
    return false;
  }
}
