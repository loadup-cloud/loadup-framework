package com.github.loadup.components.gateway.core.prototype.util;

/*-
 * #%L
 * loadup-components-gateway-core
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 屏蔽敏感字段工具
 */
public class MaskUtil {

    private static final Logger logger = LoggerFactory.getLogger(MaskUtil.class);

    /**
     * 屏蔽后的字符串，用来打印敏感日志
     */
    public static String mask(String origStr, String shieldTypeStr) {
        //        ShieldMethodEnum shieldMethodEnum = null;
        //        try {
        //            shieldMethodEnum = ShieldMethodEnum.valueOf(shieldTypeStr);
        //        } catch (Exception e) {
        //            LogUtil.warn(logger, e, "MaskUtil.mask fail! shieldTypeStr is ", shieldTypeStr);
        //            shieldMethodEnum = ShieldMethodEnum.ALL;
        //        }
        //
        //        Shield shield = ShieldFactory.getShielder(shieldMethodEnum);
        //
        //        return shield.shield(origStr, new String[] {});
        return "";
    }

    /**
     * 屏蔽前6后4
     */
    public static String shieldTopSixLastFour(String origStr) {
        //eg: cardno should be shielded in log
        if (origStr.length() <= 10) {
            return defaultHide(origStr);
        }
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(origStr.substring(0, 6)).append("****")
                .append(origStr.substring(origStr.length() - 4)).toString();
    }

    /**
     * 默认方式，分成三份，中间加密
     */
    public static String defaultHide(String sensitiveData) {

        return "***";
    }

}
