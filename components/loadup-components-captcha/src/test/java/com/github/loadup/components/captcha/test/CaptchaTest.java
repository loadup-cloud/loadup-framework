package com.github.loadup.components.captcha.test;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.captcha.enmus.CaptchaTypeEnum;
import com.github.loadup.components.captcha.model.CaptchaResult;
import com.github.loadup.components.captcha.service.CaptchaFactory;
import com.github.loadup.components.captcha.service.CaptchaService;

import java.io.File;
import java.util.UUID;
import javax.annotation.Resource;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CaptchaTest {
    @Resource
    private CaptchaFactory captchaFactory;

    @SneakyThrows
    @Test
    void testGenerateCaptcha() {
        for (CaptchaTypeEnum typeEnum : CaptchaTypeEnum.values()) {
            String filePath = null;
            try {
                String uuid = UUID.randomUUID().toString();
                CaptchaService captchaService = captchaFactory.getCaptchaService(typeEnum);
                CaptchaResult generate = captchaService.generate(uuid);
                String value = CaptchaFactory.CACHE_MAP.get(uuid);
                filePath = "./target" + File.separator + value + "."
                        + typeEnum.getCode().substring(0, 3).toLowerCase();
                generate.toFile(filePath);
                Assertions.assertTrue(new File(filePath).exists());
                Assertions.assertTrue(captchaService.validate(uuid, value));
            } finally {
                System.out.println(filePath);
                if (filePath != null) {
                    // Files.deleteIfExists(Paths.get(filePath));
                }
            }
        }
    }
}
