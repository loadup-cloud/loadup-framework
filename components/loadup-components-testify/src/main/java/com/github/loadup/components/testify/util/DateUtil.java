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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时期处理类
 *
 * 
 *
 */
public class DateUtil {
    public final static long ONE_DAY_SECONDS = 86400;
    public final static long ONE_DAY_MILL_SECONDS = 86400000;
    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("GMT");
    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    public final static String monthFormat = "yyyyMM";
    public final static String chineseDtFormat = "yyyy年MM月dd日";
    public final static String noSecondFormat = "yyyy-MM-dd HH:mm";
    /**
     * 完整时间 yyyy-MM-dd HH:mm:ss
     */
    public static final String simple = "yyyy-MM-dd HH:mm:ss";
    /**
     * 年月日 yyyy-MM-dd
     */
    public static final String dbSimple = "yyyy-MM-dd";
    /**
     * 年月日(中文) yyyy年MM月dd日
     */
    public static final String dtSimpleChinese = "yyyy年MM月dd日";
    public static final String week = "EEEE";
    /**
     * 年月日(无下划线) yyyyMMdd
     */
    public static final String dtShort = "yyyyMMdd";
    /**
     * 年月日时分秒(无下划线) yyyyMMddHHmmss
     */
    public static final String dtLong = "yyyyMMddHHmmss";
    /**
     * 时分秒 HH:mm:ss
     */
    public static final String hmsFormat = "HH:mm:ss";
    /**
     * 年-月-日 小时:分钟 yyyy-MM-dd HH:mm
     */
    public static final String simpleFormat = "yyyy-MM-dd HH:mm";
    // 年月日时分秒毫秒(无下划线)
    public static final String dtLongMill = "yyyyMMddHHmmssS";
    // 时分秒
    public final static String timeFormat = "HHmmss";
    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    /**
     * yyyyMMdd
     */
    public final static String SHORT_FORMAT = "yyyyMMdd";

    /**
     * yyyyMMddHHmmss
     */
    public final static String LONG_FORMAT = "yyyyMMddHHmmss";

    /**
     * yyyy-MM-dd
     */
    public final static String WEB_FORMAT = "yyyy-MM-dd";

    /**
     * HHmmss
     */
    public final static String TIME_FORMAT = "HHmmss";

    /**
     * yyyyMM
     */
    public final static String MONTH_FORMAT = "yyyyMM";

    /**
     * yyyy年MM月dd日
     */
    public final static String CHINA_FORMAT = "yyyy年MM月dd日";

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public final static String LONG_WEB_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm
     */
    public final static String LONG_WEB_FORMAT_NO_SEC = "yyyy-MM-dd HH:mm";

    /**
     * 日期对象解析成日期字符串基础方法，可以据此封装出多种便捷的方法直接使用
     *
     * @param date   待格式化的日期对象
     * @param format 输出的格式
     * @return 格式化的字符串
     */
    public static String format(Date date, String format) {
        if (date == null || StringUtils.isBlank(format)) {
            return StringUtils.EMPTY;
        }

        return new SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE).format(date);
    }

    /**
     * 格式化当前时间
     *
     * @param format 输出的格式
     * @return
     */
    public static String formatCurrent(String format) {
        if (StringUtils.isBlank(format)) {
            return StringUtils.EMPTY;
        }

        return format(new Date(), format);
    }

    /**
     * 日期字符串解析成日期对象基础方法，可以在此封装出多种便捷的方法直接使用
     *
     * @param dateStr 日期字符串
     * @param format  输入的格式
     * @return 日期对象
     * @throws ParseException
     */
    public static Date parse(String dateStr, String format) throws ParseException {
        if (StringUtils.isBlank(format)) {
            throw new ParseException("format can not be null.", 0);
        }

        if (dateStr == null || dateStr.length() < format.length()) {
            throw new ParseException("date string's length is too small.", 0);
        }

        return new SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE).parse(dateStr);
    }

    /**
     * 日期字符串格式化基础方法，可以在此封装出多种便捷的方法直接使用
     *
     * @param dateStr   日期字符串
     * @param formatIn  输入的日期字符串的格式
     * @param formatOut 输出日期字符串的格式
     * @return 已经格式化的字符串
     * @throws ParseException
     */
    public static String format(String dateStr, String formatIn, String formatOut)
            throws ParseException {

        Date date = parse(dateStr, formatIn);
        return format(date, formatOut);
    }

    /**
     * 把日期对象按照<code>yyyyMMdd</code>格式解析成字符串
     *
     * @param date 待格式化的日期对象
     * @return 格式化的字符串
     */
    public static String formatShort(Date date) {
        return format(date, SHORT_FORMAT);
    }

    /**
     * 把日期字符串按照<code>yyyyMMdd</code>格式，进行格式化
     *
     * @param dateStr  待格式化的日期字符串
     * @param formatIn 输入的日期字符串的格式
     * @return 格式化的字符串
     */
    public static String formatShort(String dateStr, String formatIn) throws ParseException {
        return format(dateStr, formatIn, SHORT_FORMAT);
    }

    /**
     * 把日期对象按照<code>yyyy-MM-dd</code>格式解析成字符串
     *
     * @param date 待格式化的日期对象
     * @return 格式化的字符串
     */
    public static String formatWeb(Date date) {
        return format(date, WEB_FORMAT);
    }

    /**
     * 把日期字符串按照<code>yyyy-MM-dd</code>格式，进行格式化
     *
     * @param dateStr  待格式化的日期字符串
     * @param formatIn 输入的日期字符串的格式
     * @return 格式化的字符串
     * @throws ParseException
     */
    public static String formatWeb(String dateStr, String formatIn) throws ParseException {
        return format(dateStr, formatIn, WEB_FORMAT);
    }

    /**
     * 把日期对象按照<code>yyyyMM</code>格式解析成字符串
     *
     * @param date 待格式化的日期对象
     * @return 格式化的字符串
     */
    public static String formatMonth(Date date) {

        return format(date, MONTH_FORMAT);
    }

    /**
     * 把日期对象按照<code>HHmmss</code>格式解析成字符串
     *
     * @param date 待格式化的日期对象
     * @return 格式化的字符串
     */
    public static String formatTime(Date date) {
        return format(date, TIME_FORMAT);
    }

    /**
     * 获取yyyyMMddHHmmss+n位随机数格式的时间戳
     *
     * @param n 随机数位数
     * @return
     */
    public static String getTimestamp(int n) {
        return formatCurrent(LONG_FORMAT) + RandomStringUtils.randomNumeric(n);
    }

    /**
     * 取得新的日期
     *
     * @param date1 日期
     * @param days  天数
     * @return 新的日期
     */
    public static Date addDays(Date date1, long days) {
        return addSeconds(date1, days * ONE_DAY_SECONDS);
    }

    /**
     * 计算当前时间几小时之后的时间
     *
     * @param date
     * @param hours
     * @return
     */
    public static Date addHours(Date date, long hours) {
        return addMinutes(date, hours * 60);
    }

    /**
     * 计算当前时间几分钟之后的时间
     *
     * @param date
     * @param minutes
     * @return
     */
    public static Date addMinutes(Date date, long minutes) {
        return addSeconds(date, minutes * 60);
    }

    /**
     * @param date1
     * @param secs
     * @return
     */

    public static Date addSeconds(Date date1, long secs) {
        return new Date(date1.getTime() + (secs * 1000));
    }

    /**
     * 计算日期差值
     *
     * @param beforDate
     * @param afterDate
     * @return int（天数）
     */
    public static final int calculateDecreaseDate(String beforDate, String afterDate)
            throws ParseException {
        Date date1 = getFormat(dbSimple).parse(beforDate);
        Date date2 = getFormat(dbSimple).parse(afterDate);
        long decrease = getDateBetween(date1, date2) / 1000 / 3600 / 24;
        int dateDiff = (int) decrease;
        return dateDiff;
    }

    /**
     * 判断日期合法性<pre>
     * 1.日期格式为YYYYMMDD
     * 2.日期存在
     *
     * 例如：
     * 2010a024      ---->     false
     * 20100631      ---->     false
     * 20101313      ---->     false
     * 20100809      ---->     true
     * </pre>
     *
     * @param dateStr
     * @return
     */
    public static boolean checkDateValid(String dateStr) {

        Date tmpDate = null;
        try {
            tmpDate = shortstring2Date(dateStr);
        } catch (ParseException e) {
            return false;
        }

        if (tmpDate != null) {
            if (StringUtils.equals(shortDate(tmpDate), dateStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验start与end相差的天数，是否满足end-start lessEqual than days
     *
     * @param start
     * @param end
     * @param days
     * @return
     */
    public static boolean checkDays(Date start, Date end, int days) {
        int g = countDays(start, end);

        return g <= days;
    }

    /**
     * 校验输入的时间格式是否合法，但不需要校验时间一定要是8位的
     *
     * @param statTime
     * @return alahan add 20050901
     */
    public static boolean checkTime(String statTime) {
        if (statTime.length() > 8) {
            return false;
        }

        String[] timeArray = statTime.split(":");

        if (timeArray.length != 3) {
            return false;
        }

        for (int i = 0; i < timeArray.length; i++) {
            String tmpStr = timeArray[i];

            try {
                Integer tmpInt = new Integer(tmpStr);

                if (i == 0) {
                    if ((tmpInt.intValue() > 23) || (tmpInt.intValue() < 0)) {
                        return false;
                    } else {
                        continue;
                    }
                }

                if ((tmpInt.intValue() > 59) || (tmpInt.intValue() < 0)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    public static String convert(String dateString, DateFormat formatIn, DateFormat formatOut) {
        try {
            Date date = formatIn.parse(dateString);

            return formatOut.format(date);
        } catch (ParseException e) {
            logger.warn("convert() --- orign date error: " + dateString);
            return "";
        }
    }

    public static String convert2ChineseDtFormat(String dateString) {
        DateFormat df1 = getNewDateFormat(dtShort);
        DateFormat df2 = getNewDateFormat(chineseDtFormat);

        return convert(dateString, df1, df2);
    }

    public static String convert2WebFormat(String dateString) {
        DateFormat df1 = getNewDateFormat(dtShort);
        DateFormat df2 = getNewDateFormat(dbSimple);

        return convert(dateString, df1, df2);
    }

    public static String convertFromWebFormat(String dateString) {
        DateFormat df1 = getNewDateFormat(dtShort);
        DateFormat df2 = getNewDateFormat(dbSimple);

        return convert(dateString, df2, df1);
    }

    /**
     * 返回日期相差天数，向下取整数
     *
     * @param dateStart 一般前者小于后者dateEnd
     * @param dateEnd
     * @return
     */
    public static int countDays(Date dateStart, Date dateEnd) {
        if ((dateStart == null) || (dateEnd == null)) {
            return -1;
        }

        return (int) ((dateEnd.getTime() - dateStart.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * add by daizhixia 20050808 取得相差的天数
     *
     * @param startDate 格式为 2008-08-01
     * @param endDate   格式为 2008-08-01
     * @return
     */
    public static long countDays(String startDate, String endDate) {
        Date tempDate1 = null;
        Date tempDate2 = null;
        long days = 0;

        try {
            tempDate1 = string2Date(startDate);

            tempDate2 = string2Date(endDate);
            days = (tempDate2.getTime() - tempDate1.getTime()) / (1000 * 60 * 60 * 24);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    /**
     * 判断参date上min分钟后，是否小于当前时间
     *
     * @param date
     * @param min
     * @return
     */
    public static boolean dateLessThanNowAddMin(Date date, long min) {
        return addMinutes(date, min).before(new Date());

    }

    /**
     * @param date1
     * @param date2
     * @param format
     * @return
     */
    public static boolean dateNotLessThan(String date1, String date2, DateFormat format) {
        try {
            Date d1 = format.parse(date1);
            Date d2 = format.parse(date2);

            if (d1.before(d2)) {
                return false;
            } else {
                return true;
            }
        } catch (ParseException e) {
            logger.debug("dateNotLessThan() --- ParseException(" + date1 + ", " + date2 + ")");
            return false;
        }
    }

    /**
     * 把日期类型的日期换成数字类型 YYYYMMDD类型
     *
     * @param date
     * @return
     */
    public static final Long dateToNumber(Date date) {
        if (date == null) {
            return null;
        }

        Calendar c = Calendar.getInstance();

        c.setTime(date);

        String month;
        String day;

        if ((c.get(Calendar.MONTH) + 1) >= 10) {
            month = "" + (c.get(Calendar.MONTH) + 1);
        } else {
            month = "0" + (c.get(Calendar.MONTH) + 1);
        }

        if (c.get(Calendar.DATE) >= 10) {
            day = "" + c.get(Calendar.DATE);
        } else {
            day = "0" + c.get(Calendar.DATE);
        }

        String number = c.get(Calendar.YEAR) + "" + month + day;

        return new Long(number);
    }

    /**
     * @param strDate yyyyMMdd
     * @return yyyy-MM-dd
     */
    public static final String dtFromShortToSimpleStr(String strDate) {
        if (null != strDate) {
            Date date;
            try {
                date = shortstring2Date(strDate);
            } catch (ParseException e) {
                date = null;
            }
            if (null != date) {
                return dtSimpleFormat(date);
            }
        }
        return "";
    }

    /**
     * yyyyMMddHHmmssS
     *
     * @param date
     * @return
     */
    public static final String dtLongMillFormat(Date date) {
        if (date == null) {
            return "";
        }

        return getFormat(dtLongMill).format(date);
    }

    /**
     * yyyyMM-dd
     *
     * @param date
     * @return
     */
    public static final String dtShortSimpleFormat(Date date) {
        if (date == null) {
            return "";
        }
        return getFormat(dtShort).format(date);
    }

    /**
     * yyyy年MM月dd日
     *
     * @param date
     * @return
     */
    public static final String dtSimpleChineseFormat(Date date) {
        if (date == null) {
            return "";
        }

        return getFormat(dtSimpleChinese).format(date);
    }

    /**
     * yyyy-MM-dd到 yyyy年MM月dd日 转换
     *
     * @param date
     * @return
     */
    public static final String dtSimpleChineseFormatStr(String date) throws ParseException {
        if (date == null) {
            return "";
        }

        return getFormat(dtSimpleChinese).format(string2Date(date));
    }

    /**
     * yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static final String dtSimpleFormat(Date date) {
        if (date == null) {
            return "";
        }

        return getFormat(dbSimple).format(date);
    }


    /**
     * 把日期2007/06/14转换为20070614
     *
     * @param date
     * @return
     * @method formatDateString
     */
    public static String formatDateString(String date) {
        String result = "";
        if (StringUtils.isBlank(date)) {
            return result;
        }
        if (date.length() == 10) {
            result = date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10);
        }
        return result;
    }

    /**
     * 根据类型格式化日期
     *
     * @param date
     * @param format
     * @return
     */
    public static final String formatDateString(Date date, String format) {
        if (date == null || StringUtils.isEmpty(format)) {
            return "";
        }

        return getFormat(format).format(date);
    }



    public static String formatTimeRange(Date startDate, Date endDate, String format) {
        if ((endDate == null) || (startDate == null)) {
            return null;
        }

        String rt = null;
        long range = endDate.getTime() - startDate.getTime();
        long day = range / MILLIS_PER_DAY;
        long hour = (range % MILLIS_PER_DAY) / MILLIS_PER_HOUR;
        long minute = (range % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE;

        if (range < 0) {
            day = 0;
            hour = 0;
            minute = 0;
        }

        rt = format.replaceAll("dd", String.valueOf(day));
        rt = rt.replaceAll("hh", String.valueOf(hour));
        rt = rt.replaceAll("mm", String.valueOf(minute));

        return rt;
    }

    /**
     * 获取系统日期的前一天日期，返回Date
     *
     * @return
     */
    public static Date getBeforeDate() {
        Date date = new Date();

        return new Date(date.getTime() - (ONE_DAY_MILL_SECONDS));
    }

    /**
     * add by shengyong 获取前一天 返回 dtSimple 格式字符
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getBeforeDay(Date date) throws ParseException {
        Calendar cad = Calendar.getInstance();
        cad.setTime(date);
        cad.add(Calendar.DATE, -1);
        return dtSimpleFormat(cad.getTime());
    }

    /**
     * add by shengyong 20050808 获取前一天
     *
     * @param StringDate
     * @return
     * @throws ParseException
     */
    public static String getBeforeDay(String StringDate) throws ParseException {
        Date tempDate = string2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);
        cad.add(Calendar.DATE, -1);
        return dtSimpleFormat(cad.getTime());
    }

    public static String getBeforeDayString(int days) {
        Date date = new Date(System.currentTimeMillis() - (ONE_DAY_MILL_SECONDS * days));
        DateFormat dateFormat = getNewDateFormat(dtShort);

        return getDateString(date, dateFormat);
    }

    public static String getBeforeDayString(String dateString, int days) {
        Date date;
        DateFormat df = getNewDateFormat(dtShort);

        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            date = new Date();
        }

        date = new Date(date.getTime() - (ONE_DAY_MILL_SECONDS * days));

        return df.format(date);
    }

    /**
     * 取得“X年X月X日”的日期格式
     *
     * @param date
     * @return
     */
    public static String getChineseDateString(Date date) {
        DateFormat dateFormat = getNewDateFormat(chineseDtFormat);

        return getDateString(date, dateFormat);
    }

    /**
     * 获取当前年份
     */
    public static String getCurYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * 计算时间差
     *
     * @param dBefor 首日
     * @param dAfter 尾日
     * @return 时间差(毫秒)
     */
    public static final long getDateBetween(Date dBefor, Date dAfter) {
        long lBefor = 0;
        long lAfter = 0;
        long lRtn = 0;

        /** 取得距离 1970年1月1日 00:00:00 GMT 的毫秒数 */
        lBefor = dBefor.getTime();
        lAfter = dAfter.getTime();

        lRtn = lAfter - lBefor;

        return lRtn;
    }

    /**
     * 当前时间与参数时间差
     *
     * @param dateBefore 首日
     * @return 时间差(分)
     */
    public static final int getDateBetweenNow(Date dateBefore) {
        if (dateBefore == null) {
            return 0;
        }
        return (int) (getDateBetween(dateBefore, new Date()) / 1000 / 60);
    }

    /**
     * @return 当天的时间格式化为"yyyyMMdd"
     */
    public static String getDateString(Date date) {
        DateFormat df = getNewDateFormat(dtShort);

        return df.format(date);
    }

    public static String getDateString(Date date, DateFormat dateFormat) {
        if (date == null || dateFormat == null) {
            return null;
        }

        return dateFormat.format(date);
    }

    /**
     * 获取当前时间的8位字符串：yyyyMMdd
     *
     * @return yyyyMMdd
     */
    public static String getDateToSimpleString() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat(dtShort);
        String nowStr = dateFormat.format(now);
        return nowStr;
    }

    /**
     * 获得指定时间当天起点时间
     *
     * @param date
     * @return
     */
    public static Date getDayBegin(Date date) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setLenient(false);

        String dateString = df.format(date);

        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            return date;
        }
    }

    /**
     * 获得日期是周几
     *
     * @param date
     * @return dayOfWeek
     * 
     */
    public static int getDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取输入日期的相差日期
     *
     * @param dt
     * @param idiff
     * @return
     */
    public static final String getDiffDate(Date dt, int idiff) {
        Calendar c = Calendar.getInstance();

        c.setTime(dt);
        c.add(Calendar.DATE, idiff);
        return dtSimpleFormat(c.getTime());
    }

    /**
     * 获取当前日期的日期差 now= 2005-07-19 diff = 1 -> 2005-07-20 diff = -1 -> 2005-07-18
     *
     * @param diff
     * @return
     */
    public static final String getDiffDate(int diff) {
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.DATE, diff);
        return dtSimpleFormat(c.getTime());
    }

    public static final String getDiffDate(String srcDate, String format, int diff) {
        DateFormat f = new SimpleDateFormat(format);

        try {
            Date source = f.parse(srcDate);
            Calendar c = Calendar.getInstance();

            c.setTime(source);
            c.add(Calendar.DATE, diff);
            return f.format(c.getTime());
        } catch (Exception e) {
            return srcDate;
        }
    }

    /**
     * 获取输入日期的相差日期
     *
     * @param dt
     * @param idiff
     * @return
     */
    public static final String getDiffDateDtShort(Date dt, int idiff) {
        Calendar c = Calendar.getInstance();

        c.setTime(dt);
        c.add(Calendar.DATE, idiff);
        return dtShortSimpleFormat(c.getTime());
    }

    /**
     * 获取输入日期的相差日期,转换为日，分秒
     *
     * @param dt
     * @param idiff
     * @return
     */
    public static final String getDiffDateMin(Date dt, int idiff) {
        Calendar c = Calendar.getInstance();

        c.setTime(dt);
        c.add(Calendar.DATE, idiff);
        return simpleFormat(c.getTime());
    }

    public static final Date getDiffDateTime(int diff) {
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.DATE, diff);
        return c.getTime();
    }

    /**
     * 获取当前日期的日期时间差
     *
     * @param diff
     * @param hours
     * @return
     */
    public static final String getDiffDateTime(int diff, int hours) {
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.DATE, diff);
        c.add(Calendar.HOUR, hours);
        return dtSimpleFormat(c.getTime());
    }

    /**
     * 取得两个日期的间隔天数
     *
     * @param one
     * @param two
     * @return 间隔天数
     */
    public static long getDiffDays(Date one, Date two) {
        Calendar sysDate = new GregorianCalendar();

        sysDate.setTime(one);

        Calendar failDate = new GregorianCalendar();

        failDate.setTime(two);
        return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / (24 * 60 * 60 * 1000);
    }

    public static long getDiffMinutes(Date one, Date two) {
        Calendar sysDate = new GregorianCalendar();

        sysDate.setTime(one);

        Calendar failDate = new GregorianCalendar();

        failDate.setTime(two);
        return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / (60 * 1000);
    }

    /**
     * 获取输入日期月份的相差日期
     *
     * @param dt
     * @param idiff
     * @return
     */
    public static final String getDiffMon(Date dt, int idiff) {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.MONTH, idiff);
        return dtSimpleFormat(c.getTime());
    }

    /**
     * 取得两个日期间隔秒数（日期1-日期2）
     *
     * @param one 日期1
     * @param two 日期2
     * @return 间隔秒数
     */
    public static long getDiffSeconds(Date one, Date two) {
        Calendar sysDate = new GregorianCalendar();

        sysDate.setTime(one);

        Calendar failDate = new GregorianCalendar();

        failDate.setTime(two);
        return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / 1000;
    }

    /**
     * alahan add 20050825 获取传入时间相差的日期
     *
     * @param dt   传入日期，可以为空
     * @param diff 需要获取相隔diff天的日期 如果为正则取以后的日期，否则时间往前推
     * @return
     */
    public static String getDiffStringDate(Date dt, int diff) {
        Calendar ca = Calendar.getInstance();

        if (dt == null) {
            ca.setTime(new Date());
        } else {
            ca.setTime(dt);
        }

        ca.add(Calendar.DATE, diff);
        return dtSimpleFormat(ca.getTime());
    }

    public static String getEmailDate(Date today) {
        String todayStr;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");

        todayStr = sdf.format(today);
        return todayStr;
    }

    /**
     * 获取格式
     *
     * @param format
     * @return
     */
    public static final DateFormat getFormat(String format) {
        return new SimpleDateFormat(format);
    }

    /**
     * 获取每月的某天到月末的区间
     *
     * @param StringDate
     * @param interval
     * @return
     */
    public static Map<String, String> getLastWeek(String StringDate, int interval)
            throws ParseException {
        Map<String, String> lastWeek = new HashMap<String, String>();
        Date tempDate = shortstring2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);

        int dayOfMonth = cad.getActualMaximum(Calendar.DAY_OF_MONTH);

        cad.add(Calendar.DATE, (dayOfMonth - 1));
        lastWeek.put("endDate", shortDate(cad.getTime()));
        cad.add(Calendar.DATE, interval);
        lastWeek.put("startDate", shortDate(cad.getTime()));

        return lastWeek;
    }

    public static String getLongDateString() {
        DateFormat df = getNewDateFormat(dtLong);
        return df.format(new Date());
    }

    public static String getLongDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(dtLong);

        return getDateString(date, dateFormat);
    }

    public static DateFormat getNewDateFormat(String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);

        df.setLenient(false);
        return df;
    }

    public static String getNewFormatDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(simple);
        return getDateString(date, dateFormat);
    }

    /**
     * add by chencg 获取下一天 返回 dtSimple 格式字符
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getNextDay(Date date) throws ParseException {
        Calendar cad = Calendar.getInstance();
        cad.setTime(date);
        cad.add(Calendar.DATE, 1);
        return dtSimpleFormat(cad.getTime());
    }

    /**
     * add by daizhixia 20050808 获取下一天
     *
     * @param StringDate
     * @return
     * @throws ParseException
     */
    public static String getNextDay(String StringDate) throws ParseException {
        Date tempDate = string2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);
        cad.add(Calendar.DATE, 1);
        return dtSimpleFormat(cad.getTime());
    }

    /**
     * add by chencg 获取下一天 返回 dtshort 格式字符
     *
     * @param StringDate "20061106"
     * @return String "2006-11-07"
     * @throws ParseException
     */
    public static Date getNextDayDtShort(String StringDate) throws ParseException {
        Date tempDate = shortstring2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);
        cad.add(Calendar.DATE, 1);
        return cad.getTime();
    }

    /**
     * add by chencg 获取下一天 返回 dtshort 格式字符
     *
     * @param StringDate "20061106"
     * @return String "20061107"
     * @throws ParseException
     */
    public static String getNextDayDtShortToShort(String StringDate) throws ParseException {
        Date tempDate = shortstring2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);
        cad.add(Calendar.DATE, 1);
        return dtShortSimpleFormat(cad.getTime());
    }

    /**
     * 获取下月
     *
     * @param StringDate
     * @return
     */
    public static String getNextMon(String StringDate) throws ParseException {
        Date tempDate = shortstring2Date(StringDate);
        Calendar cad = Calendar.getInstance();

        cad.setTime(tempDate);
        cad.add(Calendar.MONTH, 1);
        return shortDate(cad.getTime());
    }

    /**
     * 获取当前时间的字符串格式，以半个小时为单位<br>
     * 当前时间2007-02-02 22:23 则返回 2007-02-02 22:00
     * 当前时间2007-02-02 22:33 则返回 2007-02-02 22:30
     *
     * @return
     */
    public static final String getNowDateForPageSelectAhead() {

        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.MINUTE) < 30) {
            cal.set(Calendar.MINUTE, 0);
        } else {
            cal.set(Calendar.MINUTE, 30);
        }
        return simpleDate(cal.getTime());
    }

    /**
     * 获取当前时间的字符串格式，以半个小时为单位<br>
     * 当前时间2007-02-02 22:23 则返回 2007-02-02 22:30
     * 当前时间2007-02-02 22:33 则返回 2007-02-02 23:00
     *
     * @return
     */
    public static final String getNowDateForPageSelectBehind() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.MINUTE) < 30) {
            cal.set(Calendar.MINUTE, 30);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
            cal.set(Calendar.MINUTE, 0);
        }
        return simpleDate(cal.getTime());
    }

    public static String getShortDateString(String strDate) {
        return getShortDateString(strDate, "-|/");
    }

    public static String getShortDateString(String strDate, String delimiter) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }

        String temp = strDate.replaceAll(delimiter, "");

        if (isValidShortDateFormat(temp)) {
            return temp;
        }

        return null;
    }

    public static String getShortFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        Date dt = new Date();

        cal.setTime(dt);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        DateFormat df = getNewDateFormat(dtShort);

        return df.format(cal.getTime());
    }

    public static String getSmsDate(Date today) {
        String todayStr;
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH:mm");

        todayStr = sdf.format(today);
        return todayStr;
    }

    public static String getTimeString(Date date) {
        DateFormat dateFormat = getNewDateFormat(hmsFormat);

        return getDateString(date, dateFormat);
    }

    /**
     * 获得毫秒级的时间戳
     *
     * @return
     */
    public static final String getTimeWithSSS() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        return sdFormat.format(date);
    }

    public static String getTodayString() {
        DateFormat dateFormat = getNewDateFormat(dtShort);

        return getDateString(new Date(), dateFormat);
    }

    public static String getTomorrowDateString(String sDate) throws ParseException {
        Date aDate = parseDateNoTime(sDate);

        aDate = addSeconds(aDate, ONE_DAY_SECONDS);

        return getDateString(aDate);
    }

    public static String getWebDateString(Date date) {
        DateFormat dateFormat = getNewDateFormat(dbSimple);

        return getDateString(date, dateFormat);
    }

    public static String getWebFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        Date dt = new Date();

        cal.setTime(dt);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        DateFormat df = getNewDateFormat(dbSimple);

        return df.format(cal.getTime());
    }

    public static String getWebNextDayString() {
        Calendar cad = Calendar.getInstance();
        cad.setTime(new Date());
        cad.add(Calendar.DATE, 1);
        return dtSimpleFormat(cad.getTime());
    }

    public static String getWebTodayString() {
        DateFormat df = getNewDateFormat(dbSimple);

        return df.format(new Date());
    }

    /**
     * 获取星期名，如“星期一”、“星期二”
     *
     * @param date
     * @return
     */
    public static final String getWeekDay(Date date) {
        return getFormat(week).format(date);
    }

    public static String getYesterDayDateString(String sDate) throws ParseException {
        Date aDate = parseDateNoTime(sDate);

        aDate = addSeconds(aDate, -ONE_DAY_SECONDS);

        return getDateString(aDate);
    }

    /**
     * 日期转换为字符串 HH:mm:ss
     *
     * @param date
     * @return
     */
    public static final String hmsFormat(Date date) {
        if (date == null) {
            return "";
        }

        return getFormat(hmsFormat).format(date);
    }

    /**
     * 加减天数
     *
     * @param aDate
     * @param days
     * @return Date
     * @author shencb 2006-12 add
     */
    public static final Date increaseDate(Date aDate, int days) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(aDate);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    public static boolean isBeforeNow(Date date) {
        if (date == null)
            return false;
        return date.compareTo(new Date()) < 0;
    }

    /**
     * 判断是否是默认工作日，一般默认工作日是星期一都星期五， 所以，这个函数本质是判断是否是星期一到星期五
     *
     * @param date
     * @return
     */
    public static final boolean isDefaultWorkingDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return !(week == 7 || week == 1);
    }

    /**
     * 是否闰年
     *
     * @param year
     * @return
     */
    public static final boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);

    }

    /**
     * 判断输入的字符串是否为合法的小时
     *
     * @param hourStr
     * @return true/false
     */
    public static boolean isValidHour(String hourStr) {
        if (!StringUtils.isEmpty(hourStr) && StringUtils.isNumeric(hourStr)) {
            int hour = new Integer(hourStr).intValue();

            if ((hour >= 0) && (hour <= 23)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断表示时间的字符是否为符合yyyyMMddHHmmss格式
     *
     * @param strDate
     * @return
     */
    public static boolean isValidLongDateFormat(String strDate) {
        if (strDate.length() != dtLong.length()) {
            return false;
        }

        try {
            Long.parseLong(strDate);
        } catch (Exception NumberFormatException) {
            return false;
        }

        DateFormat df = getNewDateFormat(dtLong);

        try {
            df.parse(strDate);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    /**
     * 判断表示时间的字符是否为符合yyyyMMddHHmmss格式
     *
     * @param strDate
     * @param delimiter
     * @return
     */
    public static boolean isValidLongDateFormat(String strDate, String delimiter) {
        String temp = strDate.replaceAll(delimiter, "");

        return isValidLongDateFormat(temp);
    }

    /**
     * 判断输入的字符串是否为合法的分或秒
     *
     * @param str
     * @return true/false
     */
    public static boolean isValidMinuteOrSecond(String str) {
        if (!StringUtils.isEmpty(str) && StringUtils.isNumeric(str)) {
            int hour = new Integer(str).intValue();

            if ((hour >= 0) && (hour <= 59)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidShortDateFormat(String strDate) {
        if (strDate.length() != dtShort.length()) {
            return false;
        }

        try {
            Integer.parseInt(strDate);
        } catch (Exception NumberFormatException) {
            return false;
        }

        DateFormat df = getNewDateFormat(dtShort);

        try {
            df.parse(strDate);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    public static boolean isValidShortDateFormat(String strDate, String delimiter) {
        String temp = strDate.replaceAll(delimiter, "");

        return isValidShortDateFormat(temp);
    }

    /**
     * 返回长日期格式（yyyyMMddHHmmss格式）
     *
     * @param Date
     * @return
     * @throws ParseException
     */
    public static final String longDate(Date Date) {
        if (Date == null) {
            return null;
        }

        return getFormat(dtLong).format(Date);
    }

    public static Date now() {
        return new Date();
    }

    public static Date parseDateLongFormat(String sDate) {
        DateFormat dateFormat = new SimpleDateFormat(dtLong);
        Date d = null;

        if ((sDate != null) && (sDate.length() == dtLong.length())) {
            try {
                d = dateFormat.parse(sDate);
            } catch (ParseException ex) {
                return null;
            }
        }

        return d;
    }

    public static Date parseDateNewFormat(String sDate) {
        DateFormat dateFormat = new SimpleDateFormat(simple);
        Date d = null;
        if ((sDate != null) && (sDate.length() == simple.length())) {
            try {
                d = dateFormat.parse(sDate);
            } catch (ParseException ex) {
                return null;
            }
        }
        return d;
    }

    public static Date parseDateNoTime(String sDate) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(dtShort);

        if ((sDate == null) || (sDate.length() < dtShort.length())) {
            throw new ParseException("length too little", 0);
        }

        if (!StringUtils.isNumeric(sDate)) {
            throw new ParseException("not all digit", 0);
        }

        return dateFormat.parse(sDate);
    }

    public static Date parseDateNoTime(String sDate, String format) throws ParseException {
        if (StringUtils.isBlank(format)) {
            throw new ParseException("Null format. ", 0);
        }

        DateFormat dateFormat = new SimpleDateFormat(format);

        if ((sDate == null) || (sDate.length() < format.length())) {
            throw new ParseException("length too little", 0);
        }

        return dateFormat.parse(sDate);
    }

    public static Date parseDateNoTimeWithDelimit(String sDate, String delimit)
            throws ParseException {
        sDate = sDate.replaceAll(delimit, "");

        DateFormat dateFormat = new SimpleDateFormat(dtShort);

        if ((sDate == null) || (sDate.length() != dtShort.length())) {
            throw new ParseException("length not match", 0);
        }

        return dateFormat.parse(sDate);
    }

    public static Date parseNoSecondFormat(String sDate) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(noSecondFormat);

        if ((sDate == null) || (sDate.length() < noSecondFormat.length())) {
            throw new ParseException("length too little", 0);
        }

        if (!StringUtils.isNumeric(sDate)) {
            throw new ParseException("not all digit", 0);
        }

        return dateFormat.parse(sDate);
    }

    /**
     * 返回短日期格式（yyyyMMdd格式）
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static final String shortDate(Date date) {
        if (date == null) {
            return null;
        }

        return getFormat(dtShort).format(date);
    }

    /**
     * 返回日期时间（Add by Gonglei）
     *
     * @param stringDate (yyyyMMdd)
     * @return
     * @throws ParseException
     */
    public static final Date shortstring2Date(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }

        return getFormat(dtShort).parse(stringDate);
    }

    /**
     * 将8位日期转换为10位日期（Add by Alcor）
     *
     * @param shortString yyyymmdd
     * @return yyyy-mm-dd
     * @throws ParseException
     */
    public static final String shortString2SimpleString(String shortString) {
        if (shortString == null) {
            return null;
        }
        try {
            return getFormat(dbSimple).format(shortstring2Date(shortString));
        } catch (Exception e) {
            return null;
        }
    }

    public static final String shortStringToString(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }
        return shortDate(strToDtSimpleFormat(stringDate));
    }

    /**
     * 时间转换字符串 2005-06-30 15:50
     *
     * @param date
     * @return
     */
    public static final String simpleDate(Date date) {
        if (date == null) {
            return "";
        }

        return getFormat(simpleFormat).format(date);
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static final String simpleFormat(Date date) {
        if (date == null) {
            return "";
        }
        return getFormat(simple).format(date);
    }

    /**
     * 字符串  2005-06-30 15:50 转换成时间
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static final Date simpleFormatDate(String dateString) throws ParseException {
        if (dateString == null) {
            return null;
        }
        return getFormat(simpleFormat).parse(dateString);
    }

    /**
     * yyyy-MM-dd 日期字符转换为时间
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static final Date string2Date(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }

        return getFormat(dbSimple).parse(stringDate);
    }

    /**
     * 将字符串按format格式转换为date类型
     *
     * @param str
     * @param format
     * @return
     */
    public static Date string2Date(String str, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * yyyy-MM-dd 日期字符转换为长整形
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static final Long string2DateLong(String stringDate) throws ParseException {
        Date d = string2Date(stringDate);

        if (d == null) {
            return null;
        }

        return new Long(d.getTime());
    }

    /**
     * 返回日期时间（Add by Sunzy）
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static final Date string2DateTime(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }

        return getFormat(simple).parse(stringDate);
    }

    /**
     * 返回日期时间（Add by wangjl）(时分秒：23:59:59)
     *
     * @param stringDate String 型
     * @return
     * @throws ParseException
     */
    public static final Date string2DateTimeBy23(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }
        if (stringDate.length() == 11)
            stringDate = stringDate + "23:59:59";
        else if (stringDate.length() == 13)
            stringDate = stringDate + ":59:59";
        else if (stringDate.length() == 16)
            stringDate = stringDate + ":59";
        else if (stringDate.length() == 10)
            stringDate = stringDate + " 23:59:59";

        return getFormat(simple).parse(stringDate);
    }

    /**
     * 返回日期时间（Add by Sunzy）
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static final Date string2DateTimeByAutoZero(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }
        if (stringDate.length() == 11)
            stringDate = stringDate + "00:00:00";
        else if (stringDate.length() == 13)
            stringDate = stringDate + ":00:00";
        else if (stringDate.length() == 16)
            stringDate = stringDate + ":00";
        else if (stringDate.length() == 10)
            stringDate = stringDate + " 00:00:00";

        return getFormat(simple).parse(stringDate);
    }

    /**
     * 返回日期时间（Add by Gonglei）
     *
     * @param stringDate (yyyyMMdd)
     * @return
     * @throws ParseException
     */
    public static final String stringToStringDate(String stringDate) {

        if (StringUtils.isEmpty(stringDate)) {
            return null;
        }

        if (stringDate.length() != 8) {
            return null;
        }

        return stringDate.substring(0, 4) + stringDate.substring(4, 6) + stringDate.substring(6, 8);
    }

    /**
     * 返回日期时间（Add by Gonglei）
     *
     * @param stringDate (yyyyMMdd)
     * @return
     * @throws ParseException
     */
    public static final String StringToStringDate(String stringDate) {
        if (stringDate == null) {
            return null;
        }

        if (stringDate.length() != 8) {
            return null;
        }

        return stringDate.substring(0, 4) + stringDate.substring(4, 6) + stringDate.substring(6, 8);
    }

    /**
     * yyyy-MM-dd HH:mm 或者yyyy-MM-dd  转换为日期格式
     *
     * @param strDate
     * @return
     */
    public static final Date strToDate(String strDate) {
        if (strToSimpleFormat(strDate) != null) {
            return strToSimpleFormat(strDate);
        } else {
            return strToDtSimpleFormat(strDate);
        }

    }

    /**
     * yyyy-mm-dd 日期格式转换为日期
     *
     * @param strDate
     * @return
     */
    public static final Date strToDtSimpleFormat(String strDate) {
        if (strDate == null) {
            return null;
        }

        try {
            return getFormat(dbSimple).parse(strDate);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * yyyy-MM-dd HH:mm 日期格式转换为日期
     *
     * @param strDate
     * @return
     */
    public static final Date strToSimpleFormat(String strDate) {
        if (strDate == null) {
            return null;
        }

        try {
            return getFormat(simpleFormat).parse(strDate);

        } catch (Exception e) {
        }

        return null;
    }

    public static boolean webDateNotLessThan(String date1, String date2) {
        DateFormat df = getNewDateFormat(dbSimple);

        return dateNotLessThan(date1, date2, df);
    }

}
