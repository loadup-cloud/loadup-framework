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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/** 测试类 Created by 王帆 on 2018-07-27 上午 10:08. */
@Slf4j
public class CaptchaTest {
  private static final Integer DEFAULT_IMAGE_WIDTH = 100;

  private static final Integer DEFAULT_IMAGE_HEIGHT = 40;

  @Test
  public void testMath() throws FileNotFoundException {
    for (int i = 0; i < 100; i++) {
      ArithmeticCaptcha captcha = new ArithmeticCaptcha(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
      log.info(captcha.text());
      captcha.out(new FileOutputStream(getPath(+i + "-math.png")));
    }
  }

  @Test
  public void test() throws Exception {
    for (int i = 0; i < 1; i++) {
      SpecCaptcha specCaptcha = new SpecCaptcha();
      specCaptcha.setLen(4);
      specCaptcha.setFont(i, 32f);
      log.info(specCaptcha.text());
      specCaptcha.out(new FileOutputStream(getPath(+i + "1.png")));
    }
  }

  @Test
  public void testGIf() throws Exception {
    for (int i = 0; i < 1; i++) {
      GifCaptcha gifCaptcha = new GifCaptcha();
      gifCaptcha.setLen(5);
      gifCaptcha.setFont(i, 32f);
      log.info(gifCaptcha.text());
      gifCaptcha.out(new FileOutputStream(getPath(+i + "2.gif")));
    }
  }

  @Test
  public void testHan() throws Exception {
    for (int i = 0; i < 1; i++) {
      ChineseCaptcha chineseCaptcha = new ChineseCaptcha();
      log.info(chineseCaptcha.text());
      chineseCaptcha.out(new FileOutputStream(getPath(+i + "3.png")));
    }
  }

  @Test
  public void testGifHan() throws Exception {
    for (int i = 0; i < 1; i++) {
      ChineseGifCaptcha chineseGifCaptcha = new ChineseGifCaptcha();
      log.info(chineseGifCaptcha.text());
      chineseGifCaptcha.out(new FileOutputStream(getPath(+i + "4.gif")));
    }
  }

  @Test
  public void testArit() throws Exception {
    for (int i = 0; i < 1; i++) {
      ArithmeticCaptcha specCaptcha = new ArithmeticCaptcha();
      specCaptcha.setLen(3);
      specCaptcha.supportAlgorithmSign(2);
      specCaptcha.setDifficulty(50);
      specCaptcha.setFont(i, 28f);
      log.info(specCaptcha.getArithmeticString() + " " + specCaptcha.text());
      specCaptcha.out(new FileOutputStream(getPath(+i + "5.png")));
    }
  }

  @Test
  public void testBase64() throws Exception {
    GifCaptcha specCaptcha = new GifCaptcha();
    log.info(specCaptcha.toBase64(""));
  }

  private static String getPath(String name) {
    URL resource = CaptchaTest.class.getResource("");
    try {
      return Paths.get(resource.toURI()).toFile().getAbsolutePath().split("test-classes")[0] + name;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
