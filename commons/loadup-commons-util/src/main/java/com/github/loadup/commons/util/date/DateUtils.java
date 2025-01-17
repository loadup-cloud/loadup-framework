package com.github.loadup.commons.util.date;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 * @author Laysan
 * @since 1.0.0
 */
public class DateUtils {
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    //======= get =================

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalDate getCurrentDate(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    public static LocalTime getCurrentTime(ZoneId zoneId) {
        return LocalTime.now(zoneId);
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDateTime getCurrentDateTime(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }

    public static ZonedDateTime getCurrentZoneDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }

    //======= format =================
    public static String format(LocalDate date, String pattern) {
        return format(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    public static String format(LocalTime time, DateTimeFormatter formatter) {
        return time.format(formatter);
    }

    public static String format(LocalTime time, String pattern) {
        return format(time, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return format(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public static String format(ZonedDateTime dateTime, String pattern) {
        return format(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(ZonedDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public static String format(Date date, String pattern) {
        return format(date, pattern, DEFAULT_ZONE);
    }

    public static String format(Date date, DateTimeFormatter formatter) {
        return format(date, formatter, DEFAULT_ZONE);
    }

    public static String format(Date date, String pattern, ZoneId zoneId) {
        return format(date, DateTimeFormatter.ofPattern(pattern), zoneId);
    }

    public static String format(Date date, DateTimeFormatter formatter, ZoneId zoneId) {
        return toLocalDateTime(date, zoneId).format(formatter);
    }

    //======= parse =================
    public static LocalDateTime parse(String str, String pattern) {
        return parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parse(String str) {
        return LocalDateTime.parse(str);
    }

    public static LocalDateTime parse(String str, DateTimeFormatter formatter) {
        return LocalDateTime.parse(str, formatter);
    }

    public static LocalDate parseLocalDate(String str) {
        return LocalDate.parse(str);
    }

    public static LocalDate parseLocalDate(String str, String pattern) {
        return parseLocalDate(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseLocalDate(String str, DateTimeFormatter formatter) {
        return LocalDate.parse(str, formatter);
    }

    public static LocalTime parseLocalTime(String str) {
        return LocalTime.parse(str);
    }

    public static LocalTime parseLocalTime(String str, String pattern) {
        return parseLocalTime(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalTime parseLocalTime(String str, DateTimeFormatter formatter) {
        return LocalTime.parse(str, formatter);
    }

    public static LocalDateTime parseLocalDateTime(String str, String pattern) {
        return parseLocalDateTime(str, pattern, DEFAULT_ZONE);
    }

    public static LocalDateTime parseLocalDateTime(String str, String pattern, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
        return localDateTime.atZone(zoneId).toLocalDateTime();
    }

    public static ZonedDateTime parseZonedDateTime(String timeStr) {
        return ZonedDateTime.parse(timeStr);
    }

    public static ZonedDateTime parseZonedDateTime(String timeStr, String pattern) {
        return parseZonedDateTime(timeStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static ZonedDateTime parseZonedDateTime(String timeStr, DateTimeFormatter formatter) {
        return ZonedDateTime.parse(timeStr, formatter);
    }

    public static Date parseDate(String dateStr, String pattern) {
        return parseDate(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static Date parseDate(String dateStr, DateTimeFormatter formatter) {
        return parseDate(dateStr, formatter, DEFAULT_ZONE);
    }

    public static Date parseDate(String dateStr, String pattern, ZoneId zoneId) {
        return parseDate(dateStr, DateTimeFormatter.ofPattern(pattern), DEFAULT_ZONE);
    }

    public static Date parseDate(String dateStr, DateTimeFormatter formatter, ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
        return toDate(dateTime, zoneId);
    }

    //======= toDate =================
    public static Date toDate(LocalDateTime dateTime) {
        return toDate(dateTime, DEFAULT_ZONE);
    }

    public static Date toDate(LocalDateTime dateTime, ZoneId zoneId) {
        Instant instant = dateTime.atZone(zoneId).toInstant();
        return Date.from(instant);
    }

    public static Date toDate(LocalDate date) {
        return toDate(date, DEFAULT_ZONE);
    }

    public static Date toDate(LocalDate date, ZoneId zoneId) {
        Instant instant = date.atStartOfDay().atZone(zoneId).toInstant();
        return Date.from(instant);
    }

    public static Date toDate(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    //======= toLocalDateTime =================

    public static LocalDateTime toLocalDateTime(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date, DEFAULT_ZONE);
    }

    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(zoneId).toLocalDateTime();
    }

    //======= toLocalDate =================
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate();
    }

    public static LocalDate toLocalDate(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(DEFAULT_ZONE).toLocalDate();
    }

    public static LocalDate toLocalDate(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDate();
    }

    //======= toLocalTime =================
    public static LocalTime toLocalTime(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(DEFAULT_ZONE).toLocalTime();
    }

    public static LocalTime toLocalTime(ZonedDateTime date) {
        return date.toLocalTime();
    }

    //======= toZonedDateTime =================
    public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(zoneId);
    }

    public static ZonedDateTime toZonedDateTime(Date date, ZoneId zoneId) {
        LocalDateTime localDateTime = toLocalDateTime(date);
        return localDateTime.atZone(zoneId);
    }

    //======= plus =================

    public static Date plusDays(Date date, int days) {
        LocalDateTime localDate = toLocalDateTime(date).plusDays(days);
        return toDate(localDate);
    }

    public static Date minusDays(Date date, int days) {
        LocalDateTime localDate = toLocalDateTime(date).minusDays(days);
        return toDate(localDate);
    }

    public static Date plus(Date date, int days, TemporalUnit temporalUnit) {
        LocalDateTime localDate = toLocalDateTime(date).plus(days, temporalUnit);
        return toDate(localDate);
    }

    public static Date minus(Date date, int days, TemporalUnit temporalUnit) {
        LocalDateTime localDate = toLocalDateTime(date).minus(days, temporalUnit);
        return toDate(localDate);
    }

    public static boolean before(Date date1, Date date2) {
        return toLocalDateTime(date1).isBefore(toLocalDateTime(date2));
    }

    public static boolean after(Date date1, Date date2) {
        return toLocalDateTime(date1).isAfter(toLocalDateTime(date2));
    }

    public static boolean equals(Date date1, Date date2) {
        return toLocalDateTime(date1).isEqual(toLocalDateTime(date2));
    }

    public static boolean equalsTime(Date date1, Date date2) {
        return toLocalTime(date1).equals(toLocalTime(date2));
    }

    public static boolean equalsDate(Date date1, Date date2) {
        return toLocalDate(date1).equals(toLocalDate(date2));
    }
}
