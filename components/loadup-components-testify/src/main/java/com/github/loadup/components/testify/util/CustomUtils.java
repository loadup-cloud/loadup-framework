package com.github.loadup.components.testify.util;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 原子数据库用户自定义方法
 *
 * @author chao.gao
 *
 */
public class CustomUtils {

    /**
     * 时间格式
     */
    public static final String dbSimple = "yyyy-MM-dd";

    /**
     * 获取系统当前日期，返回Date
     *
     * @return
     */
    public static Date getNowDate() {
        Date date = new Date();

        return new Date(date.getTime());
    }

    /**
     * 随机生成指定位数的ID(数字+字母)
     *
     * @param length
     * @return
     */
    public static String genRandomNum(int length) {

        String[] EM = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "q", "w", "e", "r", "t",
                "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x",
                "c", "v", "b", "n", "m"};

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int a = (int) (Math.random() * 36);
            sb.append(EM[a]);
        }
        return sb.toString();
    }

    /**
     * 随机生成指定位数的通用ID(日期+随机数)
     *
     * @param length
     * @return
     */
    public static String genRandomId(int length) {

        String[] EM = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        Date today = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf1.format(today);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < (length - 8); i++) {
            int a = (int) (Math.random() * 10);
            sb.append(EM[a]);
        }
        return date + sb.toString();
    }

    /**
     * 获取开始时间，string转date
     *
     * @param strDate
     * @return
     */
    public static Date getStartTime(String strDate) {
        if (strDate == null) {
            return null;
        }

        Date date = new Date();

        try {
            date = getFormat(dbSimple).parse(strDate);

        } catch (Exception e) {
        }
        return date;
    }

    /**
     * 获取结束时间，String转Date
     *
     * @param strDate
     * @return
     */
    public static Date getEndTime(String strDate) {
        if (strDate == null) {
            return null;
        }

        Date date = new Date();

        try {
            date = getFormat(dbSimple).parse(strDate);

        } catch (Exception e) {
        }
        return date;
    }

    /**
     * 时间转换
     *
     * @param format
     * @return
     */
    public static DateFormat getFormat(String format) {
        return new SimpleDateFormat(format);
    }

}
