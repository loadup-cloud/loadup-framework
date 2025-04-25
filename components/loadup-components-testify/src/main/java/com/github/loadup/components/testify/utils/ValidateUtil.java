/**

 * Copyright (c) 2004-2011 All Rights Reserved.
 */
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * 通用的格式校验工具类
 *
 * 
 * 
 * $
 */
public class ValidateUtil {
    // 手机格式的正则表达式
    public final static String REGEX_MOBILE = "^[1](3|4|5|8)[0-9]{9}$";

    // Email格式的正则表达式
    public final static String REGEX_EMAIL = "^([a-zA-Z0-9_\\.\\-\\+])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";

    // 手机6位数字校验码
    public final static String REGUX_VALIDATE_CODE = "^\\d{6}$";

    // 手机6位数字校验码，字母与数字混合
    public final static String REGUX_VALIDATE_CODE_EX = "^[a-zA-Z0-9]{6}$";

    // 16位纯数字流水号
    public final static String REGEX_VALIDATE_BIZ_NO = "^\\d{16}$";

    // 电话号码，只能由数字和"-"组成，6到20位
    public final static String REGEX_PHONE = "^[0-9,-]{5,20}$";

    // 邮政编码
    public final static String POST = "^[a-zA-Z0-9,-]{1,12}$";

    // 用户姓名，只能由中文汉字、大写英文字母或.构成
    public final static String REGEX_USER_NAME = "^([\u4e00-\u9fa5|A-Z]+\\s*\\.?\\s*)+[\u4e00-\u9fa5|A-Z]$";

    // 银行卡全卡号必须是15到19位数字
    public final static String REGEX_BANK_CARD_NO = "^\\d{15,19}$";

    // 纯数字
    public final static String NUM = "^(\\d)+$";

    // 银行卡全卡号必须是13-16位数字
    public final static String REGEX_CREDIT_CARD_NO = "^\\d{13,16}$";

    // CVV2是3-4位数字
    public final static String REGEX_CVV2 = "^\\d{3}$";

    /**
     * 密码解密时间戳(local)
     */
    public static final String DATE_TIME_FLAG = "DATE_TIME_FLAG";

    /**
     * 判断输入的字符串是否和传入的与此同时匹配
     *
     * @param src 待判断的输入字符串
     * @param reg 正则表达式
     * @return True:输入的字符串是否和传入的正则匹配 False:输入的字符串是否和传入的正则不匹配
     */
    public static boolean isValidReg(String src, String reg) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern p = Pattern.compile(reg);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 判断输入的字符串是否是合法用户姓名,长度未限制
     *
     * @param src 待判断的输入字符串
     * @return True:是合法的用户姓名;False:不是合法的用户姓名
     */
    public static boolean isValidUserName(String src) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern p = Pattern.compile(REGEX_USER_NAME);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 判断输入的字符串是否是合法email格式,长度未限制
     *
     * @param src 待判断的输入字符串
     * @return True:是合法的email;False:不是合法的email
     */
    public static boolean isValidEmail(String src) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern p = Pattern.compile(REGEX_EMAIL);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 判断输入的字符串是否是手机号码格式
     *
     * @param src 待判断的输入字符串
     * @return True:是合法的手机号码;False:不是合法的手机号码
     */
    public static boolean isValidMobile(String src) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern p = Pattern.compile(REGEX_MOBILE);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 判断输入的字符串是否是合法的固话
     *
     * @param src 待判断的输入字符串
     * @return True:合法;False:不合法
     */
    public static boolean isValidPhone(String src) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern p = Pattern.compile(REGEX_PHONE);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 判断是否是合法的流水号，标准:流水号为16位纯数字
     *
     * @param src 待判断的输入字符串
     * @return true是合法的流水号;false表示不是合法的流水号
     */
    public static boolean isValidBizNo(String src) {
        // 输入为空则直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }

        Pattern p = Pattern.compile(REGEX_VALIDATE_BIZ_NO);
        Matcher pm = p.matcher(src);
        return pm.matches();
    }

    /**
     * 第二种判断是否是合法的金额的方法，利用转化为Money对象的正确与否。注意:<br/>
     * 这样不能判别,分隔的多个数字情形，只是全部忽略掉。<li>
     *
     * @param src 待判断的输入字符串
     * @return true表示是合法的金额;false表示不是合法的金额
     */
    public static boolean isValidMoney2(String src) {
        // 无效的输入，直接返回false
        if (StringUtils.isBlank(src)) {
            return false;
        }

        String inSrc = StringUtils.replaceChars(src, ",", "");

        // 全部是,的，直接返回false
        if (StringUtils.isBlank(inSrc)) {
            return false;
        }

        try {
//            new Money(inSrc);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
