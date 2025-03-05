// package com.github.loadup.commons.util.date;
//
/// *-
// * #%L
// * loadup-commons-util
// * %%
// * Copyright (C) 2022 - 2024 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
/// **
// * @author Laysan
// * @since 1.0.0
// */
//
//
// import java.time.*;
// import java.time.temporal.ChronoUnit;
// import java.util.Date;
//
// public class DateUtilsTest {
//
//    private final ZoneId localZone = ZoneId.of("+8");
//
//    @Test
//    public void testGetCurrentDate() {
//        LocalDate currentDate = DateUtils.getCurrentDate(localZone);
//        Assert.assertNotNull(currentDate);
//    }
//
//    @Test
//    public void testGetCurrentTime() {
//        LocalTime currentTime = DateUtils.getCurrentTime(localZone);
//        Assert.assertNotNull(currentTime);
//    }
//
//    @Test
//    public void testGetCurrentDateTime() {
//        LocalDateTime currentDateTime = DateUtils.getCurrentDateTime(localZone);
//        Assert.assertNotNull(currentDateTime);
//    }
//
//    @Test
//    public void testGetCurrentZoneDateTime() {
//        ZonedDateTime currentZoneDateTime = DateUtils.getCurrentZoneDateTime(localZone);
//        Assert.assertNotNull(currentZoneDateTime);
//    }
//
//    @Test
//    public void testFormatLocalDate() {
//        LocalDate date = LocalDate.of(2023, 8, 15);
//        String formattedDate = DateUtils.format(date, "yyyy-MM-dd");
//        Assert.assertEquals(formattedDate, "2023-08-15");
//    }
//
//    @Test
//    public void testFormatLocalTime() {
//        LocalTime time = LocalTime.of(10, 30);
//        String formattedTime = DateUtils.format(time, "HH:mm:ss");
//        Assert.assertEquals(formattedTime, "10:30:00");
//    }
//
//    @Test
//    public void testFormatLocalDateTime() {
//        LocalDateTime dateTime = LocalDateTime.of(2023, 8, 15, 10, 30);
//        String formattedDateTime = DateUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");
//        Assert.assertEquals(formattedDateTime, "2023-08-15 10:30:00");
//    }
//
//    @Test
//    public void testFormatZonedDateTime() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        String formattedZonedDateTime = DateUtils.format(zonedDateTime, "yyyy-MM-dd HH:mm:ssXXX");
//        Assert.assertEquals(formattedZonedDateTime, "2023-08-15 10:30:00+08:00");
//    }
//
//    @Test
//    public void testFormatDate() {
//        Date date = Date.from(Instant.parse("2023-08-15T12:00:00Z"));
//        String formattedDate = DateUtils.format(date, "yyyy-MM-dd HH:mm:ss", localZone);
//        Assert.assertEquals(formattedDate, "2023-08-15 20:00:00"); // Adjusted to local time
//    }
//
//    @Test
//    public void testParseLocalDate() {
//        LocalDate parsedDate = DateUtils.parseLocalDate("2023-08-15", "yyyy-MM-dd");
//        LocalDate expectedDate = LocalDate.of(2023, 8, 15);
//        Assert.assertEquals(parsedDate, expectedDate);
//    }
//
//    @Test
//    public void testParseLocalDateTime() {
//        LocalDateTime parsedDateTime = DateUtils.parseLocalDateTime("2023-08-15 10:30:00", "yyyy-MM-dd HH:mm:ss",
// localZone);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 8, 15, 10, 30);
//        Assert.assertEquals(parsedDateTime, expectedDateTime);
//    }
//
//    @Test
//    public void testParseZonedDateTime() {
//        ZonedDateTime parsedZonedDateTime = DateUtils.parseZonedDateTime("2023-08-15T10:30:00+08:00",
// "yyyy-MM-dd'T'HH:mm:ssXXX");
//        ZonedDateTime expectedZonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        Assert.assertEquals(parsedZonedDateTime, expectedZonedDateTime);
//    }
//
//    @Test
//    public void testParseDate() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        Date expectedDate = Date.from(Instant.parse("2024-07-24T00:00:00Z"));
//        Assert.assertEquals(date, expectedDate);
//    }
//
//    @Test
//    public void testPlusDays() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        Date futureDate = DateUtils.plusDays(date, 5);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 29, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(futureDate, localZone), expectedDateTime);
//    }
//
//    @Test
//    public void testMinusDays() {
//        Date date = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Date pastDate = DateUtils.minusDays(date, 3);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 8, 12, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(pastDate, localZone), expectedDateTime);
//    }
//
//    @Test
//    public void testPlusAndMinusTemporalUnit() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        Date futureDate = DateUtils.plus(date, 1, ChronoUnit.WEEKS);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 31, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(futureDate, localZone), expectedDateTime);
//
//        Date pastDate = DateUtils.minus(date, 2, ChronoUnit.MONTHS);
//        LocalDateTime expectedPastDateTime = LocalDateTime.of(2024, 5, 24, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(pastDate, localZone), expectedPastDateTime);
//    }
//
//    @Test
//    public void testBefore() {
//        Date date1 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Date date2 = Date.from(Instant.parse("2023-08-20T00:00:00Z"));
//        Assert.assertTrue(DateUtils.before(date1, date2));
//    }
//
//    @Test
//    public void testAfter() {
//        Date date1 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Date date2 = Date.from(Instant.parse("2023-08-10T00:00:00Z"));
//        Assert.assertTrue(DateUtils.after(date1, date2));
//    }
//
//    @Test
//    public void testEquals() {
//        Date date1 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Date date2 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Assert.assertTrue(DateUtils.equals(date1, date2));
//    }
//
//    @Test
//    public void testEqualsTime() {
//        Date date1 = Date.from(Instant.parse("2023-08-15T10:30:00Z"));
//        Date date2 = Date.from(Instant.parse("2023-08-15T10:30:00Z"));
//        Assert.assertTrue(DateUtils.equalsTime(date1, date2));
//    }
//
//    @Test
//    public void testEqualsDate() {
//        Date date1 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Date date2 = Date.from(Instant.parse("2023-08-15T00:00:00Z"));
//        Assert.assertTrue(DateUtils.equalsDate(date1, date2));
//    }
//
//    @Test
//    public void testToDateLocalDate() {
//        LocalDate localDate = LocalDate.of(2023, 8, 15);
//        Date date = DateUtils.toDate(localDate, localZone);
//        Assert.assertNotNull(date);
//        Assert.assertEquals(DateUtils.toLocalDate(date), localDate);
//    }
//
//    @Test
//    public void testToDateLocalDateTime() {
//        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 15, 10, 30);
//        Date date = DateUtils.toDate(localDateTime, localZone);
//        Assert.assertNotNull(date);
//        Assert.assertEquals(DateUtils.toLocalDateTime(date, localZone), localDateTime);
//    }
//
//    @Test
//    public void testToDateZonedDateTime() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        Date date = DateUtils.toDate(zonedDateTime);
//        Assert.assertNotNull(date);
//        Assert.assertEquals(DateUtils.toZonedDateTime(date, localZone), zonedDateTime);
//    }
//
//    @Test
//    public void testToLocalDateTimeLocalDate() {
//        LocalDate localDate = LocalDate.of(2023, 8, 15);
//        LocalDateTime localDateTime = DateUtils.toLocalDateTime(localDate);
//        Assert.assertEquals(localDateTime.toLocalDate(), localDate);
//    }
//
//    @Test
//    public void testToLocalDateTimeDate() {
//        Date date = Date.from(Instant.parse("2023-08-15T10:30:00Z"));
//        LocalDateTime localDateTime = DateUtils.toLocalDateTime(date, localZone);
//        Assert.assertEquals(DateUtils.toDate(localDateTime, localZone), date);
//    }
//
//    @Test
//    public void testToLocalDateTimeZonedDateTime() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        LocalDateTime localDateTime = DateUtils.toLocalDateTime(zonedDateTime);
//        Assert.assertEquals(DateUtils.toZonedDateTime(localDateTime, zonedDateTime.getZone()), zonedDateTime);
//    }
//
//    @Test
//    public void testToLocalDateLocalDateTime() {
//        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 15, 10, 30);
//        LocalDate localDate = DateUtils.toLocalDate(localDateTime);
//        Assert.assertEquals(localDate, localDateTime.toLocalDate());
//    }
//
//    @Test
//    public void testToLocalDateDate() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        LocalDate localDate = DateUtils.toLocalDate(date);
//        Date date2 = Date.from(Instant.parse("2024-07-23T16:00:00Z"));
//        Assert.assertEquals(DateUtils.toDate(localDate, localZone), date2);
//    }
//
//    @Test
//    public void testToLocalDateZonedDateTime() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        LocalDateTime localDate = DateUtils.toLocalDateTime(zonedDateTime);
//        Assert.assertEquals(DateUtils.toZonedDateTime(localDate, zonedDateTime.getZone()), zonedDateTime);
//    }
//
//    @Test
//    public void testToLocalTimeDate() {
//        Date date = Date.from(Instant.parse("2023-08-15T10:30:00Z"));
//        LocalTime localTime = DateUtils.toLocalTime(date);
//        Assert.assertEquals(localTime, LocalTime.of(18, 30)); // Adjusted to local time
//    }
//
//    @Test
//    public void testToLocalTimeZonedDateTime() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 8, 15, 10, 30, 0, 0, localZone);
//        LocalTime localTime = DateUtils.toLocalTime(zonedDateTime);
//        Assert.assertEquals(localTime, LocalTime.of(10, 30));
//    }
//
//    @Test
//    public void testToZonedDateTimeLocalDateTime() {
//        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 15, 10, 30);
//        ZonedDateTime zonedDateTime = DateUtils.toZonedDateTime(localDateTime, localZone);
//        Assert.assertEquals(DateUtils.toLocalDateTime(zonedDateTime), localDateTime);
//    }
//
//
//    @Test
//    public void testPlusDaysLong() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        Date futureDate = DateUtils.plusDays(date, 5);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 29, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(futureDate, localZone), expectedDateTime);
//    }
//
//    @Test
//    public void testMinusDaysLong() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        Date pastDate = DateUtils.minusDays(date, 3);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 21, 8, 0);
//        Assert.assertEquals(DateUtils.toLocalDateTime(pastDate, localZone), expectedDateTime);
//    }
//
//    @Test
//    public void testFormatWithDatePattern() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        String formattedDate = DateUtils.format(date, "yyyy-MM-dd", localZone);
//        Assert.assertEquals(formattedDate, "2024-07-24");
//    }
//
//    @Test
//    public void testFormatWithTimePattern() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        String formattedTime = DateUtils.format(date, "HH:mm:ss", localZone);
//        Assert.assertEquals(formattedTime, "08:00:00"); // Adjusted to local time
//    }
//
//    @Test
//    public void testFormatWithDateTimePattern() {
//        Long timeStamp = 1721779200000L; // Equivalent to 2024-07-24 08:00:00 GMT
//        Date date = new Date(timeStamp);
//        String formattedDateTime = DateUtils.format(date, "yyyy-MM-dd HH:mm:ss", localZone);
//        Assert.assertEquals(formattedDateTime, "2024-07-24 08:00:00"); // Adjusted to local time
//    }
//
//    @Test
//    public void testParseWithDatePattern() {
//        String dateStr = "2024-07-24";
//        LocalDate parsedDate = DateUtils.parseLocalDate(dateStr, "yyyy-MM-dd");
//        LocalDate expectedDate = LocalDate.of(2024, 7, 24);
//        Assert.assertEquals(parsedDate, expectedDate);
//    }
//
//    @Test
//    public void testParseWithTimePattern() {
//        String timeStr = "08:00:00";
//        LocalTime parsedTime = DateUtils.parseLocalTime(timeStr, "HH:mm:ss");
//        LocalTime expectedTime = LocalTime.of(8, 0); // Adjusted to local time
//        Assert.assertEquals(parsedTime, expectedTime);
//    }
//
//    @Test
//    public void testParseWithDateTimePattern() {
//        String dateTimeStr = "2024-07-24 08:00:00";
//        LocalDateTime parsedDateTime = DateUtils.parseLocalDateTime(dateTimeStr, "yyyy-MM-dd HH:mm:ss", localZone);
//        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 24, 8, 0);
//        Assert.assertEquals(parsedDateTime, expectedDateTime);
//    }
//
//    @Test
//    public void testParseWithZonedDateTime() {
//        String dateTimeStr = "2024-07-24T08:00:00+08:00";
//        ZonedDateTime parsedZonedDateTime = DateUtils.parseZonedDateTime(dateTimeStr, "yyyy-MM-dd'T'HH:mm:ssXXX");
//        ZonedDateTime expectedZonedDateTime = ZonedDateTime.of(2024, 7, 24, 8, 0, 0, 0, localZone);
//        Assert.assertEquals(parsedZonedDateTime, expectedZonedDateTime);
//    }
//
//    // Add more tests as needed for other methods...
//
// }
//
//
