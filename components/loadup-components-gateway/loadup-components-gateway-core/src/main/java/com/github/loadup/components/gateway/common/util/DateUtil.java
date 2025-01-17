package com.github.loadup.components.gateway.common.util;

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

import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Data tools
 */
public final class DateUtil {

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
     * yyyyMMddTHH:mm:ssZ
     */
    public final static String STANDARD_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * yyyyMMdd HH:mm:ss
     */
    public final static String DATE_TIME_FORMAT = "yyyyMMdd HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public final static String LONG_WEB_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm
     */
    public final static String LONG_WEB_FORMAT_NO_SEC = "yyyy-MM-dd HH:mm";

    /**
     * yyyy-MM-dd'T'HH:mm:ss
     */
    public final static String CNAPS_STANDARD_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * ISO datetime
     */
    private final static int ISO_DATE_LENGTH = 25;

    /**
     * simple data format tool
     *
     * @throws ParseException
     */
    public static Date parse(String dateStr, String format) throws ParseException {
        return parseWithLocal(dateStr, format, null);
    }

    /**
     * simple data format tool
     *
     * @throws ParseException
     */
    public static Date parseWithLocal(String dateStr, String format, Locale locale) throws ParseException {
        if (StringUtils.isBlank(format)) {
            throw new ParseException("format can not be null.", 0);
        }

        if (StringUtils.equals(STANDARD_FORMAT, format)) {
            if (dateStr.length() >= ISO_DATE_LENGTH) {
                return verifyData(dateStr);
            }

        }

        if (locale == null) {
            return new SimpleDateFormat(format).parse(dateStr);
        } else {
            return new SimpleDateFormat(format, locale).parse(dateStr);
        }
    }

    private static Date verifyData(String dateStr) throws ParseException {
        Date date = parseISODatetime(dateStr);
        if (date == null) {
            throw new ParseException(dateStr + " is not ISO8601 format", 0);
        }
        return date;
    }

    /**
     * simple data format tool
     *
     * @throws ParseException
     */
    public static String format(String dateStr, String formatIn, String formatOut)
            throws ParseException {

        Date date = parse(dateStr, formatIn);
        return DateFormatUtils.format(date, formatOut);
    }

    /**
     * simple data format tool
     *
     * @throws ParseException
     */
    public static String format(String dateStr, String formatIn, String zoneIn, String formatOut,
                                String zoneOut) throws ParseException {
        Date date = parse(dateStr, formatIn, zoneIn);
        return format(date, formatOut, zoneOut);
    }

    /**
     * simple data format tool
     *
     * @throws ParseException
     */
    public static Date parse(String dateStr, String format, String gmtZone) throws ParseException {
        if (StringUtils.isBlank(format)) {
            throw new ParseException("format can not be null.", 0);
        }

        if (dateStr == null || dateStr.length() < format.length()) {
            throw new ParseException("date string's length is too small.", 0);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(gmtZone));
        return simpleDateFormat.parse(dateStr);
    }

    /**
     * simple data format tool
     */
    public static String format(Date date, String format, String gmtZone) {
        if (date == null || StringUtils.isBlank(format)) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(gmtZone));
        return simpleDateFormat.format(date);
    }

    /**
     * ISO datetime data formate parser
     */
    public static Date parseISODatetime(String isoDatetime) {
        if (StringUtils.isBlank(isoDatetime) || (isoDatetime.length() < ISO_DATE_LENGTH)) {
            return null;
        }
        try {
            return DateUtils.parseDate(isoDatetime);
        } catch (IllegalArgumentException e) {
            ExceptionUtil.caught(e, "DateUtil.parseXSDate exception,isoDatetime=" + isoDatetime);
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getISODatetimeString(Date date) {
        if (date != null) {
            return DateFormatUtils.format(date, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern());
        }
        return null;
    }
}
