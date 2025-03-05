package com.github.loadup.components.captcha.service;

/*-
 * #%L
 * loadup-components-captcha
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
import com.github.loadup.components.captcha.service.impl.ArithmeticCaptchaServiceImpl;
import com.github.loadup.components.captcha.service.impl.ChineseCaptchaServiceImpl;
import com.github.loadup.components.captcha.service.impl.ChineseGifCaptchaServiceImpl;
import com.github.loadup.components.captcha.service.impl.GifCaptchaServiceImpl;
import com.github.loadup.components.captcha.service.impl.SpecCaptchaServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class CaptchaFactory {
    public static final Map<String, String> CACHE_MAP = new ConcurrentHashMap<>();
    /**
     * key CaptchaTypeEnum
     */
    private Map<String, CaptchaService> captchaServiceMap = new HashMap<>();

    @Resource
    private ArithmeticCaptchaServiceImpl arithmeticCaptchaService;

    @Resource
    private SpecCaptchaServiceImpl specCaptchaService;

    @Resource
    private GifCaptchaServiceImpl gifCaptchaService;

    @Resource
    private ChineseCaptchaServiceImpl chineseCaptchaService;

    @Resource
    private ChineseGifCaptchaServiceImpl chineseGifCaptchaService;

    @PostConstruct
    private void init() {
        captchaServiceMap.put(CaptchaTypeEnum.PNG_MATH_IMG.getCode(), arithmeticCaptchaService);
        captchaServiceMap.put(CaptchaTypeEnum.PNG_IMG.getCode(), specCaptchaService);
        captchaServiceMap.put(CaptchaTypeEnum.GIF_IMG.getCode(), gifCaptchaService);
        captchaServiceMap.put(CaptchaTypeEnum.PNG_CN_IMG.getCode(), chineseCaptchaService);
        captchaServiceMap.put(CaptchaTypeEnum.GIF_CN_IMG.getCode(), chineseGifCaptchaService);
    }

    public CaptchaService getCaptchaService(CaptchaTypeEnum type) {
        return captchaServiceMap.get(type.getCode());
    }
}
