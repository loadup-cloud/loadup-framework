package io.github.loadup.components.captcha.config;

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

import io.github.loadup.components.captcha.ArithmeticCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lengleng
 * @date 2020/7/31
 */
@Controller
public class CaptchaEndpoint {

    private final CaptchaProperties properties;

    /**
     * 生成验证码
     *
     * @param response 响应流
     */
    @GetMapping("${captcha.create.path:/create}")
    public void create(HttpServletResponse response) throws IOException {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(properties.getWidth(), properties.getHeight());
        // 设置响应头
        response.setContentType(captcha.getContentType());
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 转换流信息写出
        captcha.out(response.getOutputStream());
    }

    public CaptchaEndpoint(CaptchaProperties properties) {
        this.properties = properties;
    }
}
