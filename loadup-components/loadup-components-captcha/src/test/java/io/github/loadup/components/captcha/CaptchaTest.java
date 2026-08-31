package io.github.loadup.components.captcha;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试类 Created by 王帆 on 2018-07-27 上午 10:08.
 */
public class CaptchaTest {
    private static final Logger log = LoggerFactory.getLogger(CaptchaTest.class);

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
