/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomChinese {

    private static final Logger logger = LoggerFactory.getLogger(RandomChinese.class);
    private static Long i = 0l;
    private static Random random = null;

    private static Random getRandomInstance() {
        synchronized (i) {
            if (random == null) {
                random = new Random(new Date().getTime());
            }
        }
        return random;
    }

    /**
     * 获取一个随机中文字符
     */
    public static String getChinese() {
        String str = null;
        int highPos, lowPos;
        Random random = getRandomInstance();
        highPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = 161 + Math.abs(random.nextInt(93));
        byte[] b = new byte[2];
        b[0] = (new Integer(highPos)).byteValue();
        b[1] = (new Integer(lowPos)).byteValue();
        try {
            str = new String(b, "GB2312");
        } catch (UnsupportedEncodingException e) {
            // 防止出现异常返回str为空
            str = "异";
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            // 防止出现未知异常未捕获而返回null
            str = "未";
            logger.error(e.getMessage(), e);
        }
        return str;
    }

    /**
     * 获取固定长度的中文字符
     */
    public static String getFixedLengthChinese(int length) {
        String str = "";
        for (int i = length; i > 0; i--) {
            str = str + getChinese();
        }
        return str;
    }

    /**
     * 获取随机长度的中文字符
     */
    public static String getRandomLengthChinese(int start, int end) {
        if (start > end) {
            int middle = start;
            start = end;
            end = middle;
        }
        String str = "";
        int length = new Random().nextInt(end + 1);
        if (length < start) {
            str = getRandomLengthChinese(start, end);
        } else {
            for (int i = 0; i < length; i++) {
                str = str + getChinese();
            }
        }
        return str;
    }
}
