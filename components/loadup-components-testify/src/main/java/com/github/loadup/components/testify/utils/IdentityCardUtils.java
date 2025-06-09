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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geyonglong.gyl
 *
 */
public class IdentityCardUtils {
    /**
     * 身份证号的第0位
     */
    public static final int ZERO = 0;

    /**
     * 身份证号的第4位
     */
    public static final int FOUR = 4;

    /**
     * 身份证号的第6位
     */
    public static final int SIX = 6;

    /**
     * 身份证号的第8位
     */
    public static final int EIGHT = 8;

    /**
     * 身份证号的第10位
     */
    public static final int TEN = 10;

    /**
     * 身份证号的第12位
     */
    public static final int TWELVE = 12;

    /**
     * 身份证号的第14位
     */
    public static final int FOURTEEN = 14;

    /**
     * 身份证号的第15位
     */
    public static final int FIFTEEN = 15;

    /**
     * 身份证号的第16位
     */
    public static final int SIXTEEN = 16;

    /**
     * 身份证号的第17位
     */
    public static final int SEVENTEEN = 17;

    /**
     * 十九世纪
     */
    public static final String NINETEEN = "19";

    /**
     * 身份证号模式
     */
    public static final String ID_CARD_NUMBER_REG = "^((([0-9]){15})|(([0-9]){17}[0-9Xx]{1}))$";

    /**
     * 18位身份证最后的附加位
     */
    public static final String OVERHEAD_BIT = "X";

    /**
     * 十九世纪
     */
    public static final int THE_19_CENTURY = 1900;

    /**
     * 男性的标识
     */
    private static final Set<String> MALE_FLAGS = new HashSet<String>();

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityCardUtils.class);
    private static int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2}; // 十七位数字本体码权重
    private static char[] VALIDATE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'}; // mod11,对应校验码字符值

    // 初始化男性标识集合
    static {
        MALE_FLAGS.add("1");
        MALE_FLAGS.add("3");
        MALE_FLAGS.add("5");
        MALE_FLAGS.add("7");
        MALE_FLAGS.add("9");
    }

    /**
     * 根据前17位计算18位身份证号码的最后一位
     *
     * @param idCardNumber17
     * @return
     */
    public static char getOverHeadBit(String idCardNumber17) {
        int sum = 0;
        int mode = 0;
        for (int i = 0; i < idCardNumber17.length() - 1; i++) {
            sum = sum + Integer.parseInt(String.valueOf(idCardNumber17.charAt(i))) * WEIGHT[i];
        }
        mode = sum % 11;
        return VALIDATE[mode];
    }

    /**
     * 校验身份证号码的长度是否有效
     *
     * @param idCardNumber 身份证号码
     * @return
     */
    public static boolean checkIdCardNumberValid(String idCardNumber) {
        if (StringUtils.isBlank(idCardNumber)) {
            return false;
        }

        if (idCardNumber.length() != 18) {
            return false;
        }

        return true;
    }

    /**
     * 根据身份证号码提取会员的出生年月日(4位年\2位月份\2位日)
     *
     * @param idCardNumber 身份证号码
     * @return 出生年月日
     **/
    public static String getBirthday(String idCardNumber) {
        if (!checkIdCardNumberValid(idCardNumber)) {
            return null;
        }

        String year = null;
        String month = null;
        String day = null;

        String idCardNo = idCardNumber.trim();

        // 处理18位身份证
        if (idCardNo.length() == 18) {
            year = idCardNo.substring(SIX, TEN);
            month = idCardNo.substring(TEN, TWELVE);
            day = idCardNo.substring(TWELVE, FOURTEEN);
        }

        return year + month + day;
    }

    /**
     * 检查出生日期是否有效。
     *
     * @param idCardNumber 身份证号码
     * @return
     */
    public static boolean checkBirthDayValid(String idCardNumber) {
        try {
            String birthDay = getBirthday(idCardNumber);

            if (StringUtils.isBlank(birthDay)) {
                return false;
            }

            // 提取年月日
            int year = Integer.parseInt(birthDay.substring(ZERO, FOUR));
            int month = Integer.parseInt(birthDay.substring(FOUR, SIX));
            int day = Integer.parseInt(birthDay.substring(SIX, EIGHT));

            // 校验年份
            if ((year < THE_19_CENTURY) || (year > getCurrentYear())) {
                return false;
            }

            // 校验月份
            if ((month < 1) || (month > 12)) {
                return false;
            }

            // 校验日期
            Calendar cal = new GregorianCalendar();
            cal.set(year, month - 1, 1);
            if ((day < 1) || (day > cal.getActualMaximum(Calendar.DAY_OF_MONTH))) {
                return false;
            }

            return true;

        } catch (Exception e) {
            LOGGER.warn("Failed to extract birthday", e);
            return false;
        }
    }

    /**
     * 取得当前年份。
     *
     * @return
     */
    private static int getCurrentYear() {
        Calendar cal = new GregorianCalendar();

        cal.setTime(new Date());
        return cal.get(Calendar.YEAR);
    }
}
