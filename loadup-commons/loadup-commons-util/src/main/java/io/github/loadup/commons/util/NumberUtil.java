/*
 * #%L
 * Loadup Common Utils
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.commons.util;
/*
 * Copyright (c) 2023 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          https://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * <pre>
 * </pre>
 *
 *
 *
 * <ul>
 *   <li><a
 *       href="https://github.com/venusdrogon/feilong-core/wiki/one-jdk7-bug-thinking">one-jdk7-bug-thinking</a>
 * </ul>
 *
 * @author Looly
 */
public class NumberUtil {

    private static final int DEFAULT_DIV_SCALE = 10;

    // region ----- add

    /**
     *
     * @since 4.0.0
     */
    public static BigDecimal add(final Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     *
     *
     * @since 4.0.0
     */
    public static BigDecimal add(final String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringUtils.isNotBlank(value)) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    // endregion

    // region ----- sub

    /**
     *
     * @since 4.0.0
     */
    public static BigDecimal sub(final Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     *
     * @since 4.0.0
     */
    public static BigDecimal sub(final String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringUtils.isNotBlank(value)) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    // endregion

    // region ----- mul

    /**
     *
     * @since 4.0.0
     */
    public static BigDecimal mul(final Number... values) {
        if (ArrayUtils.isEmpty(values) || ArrayUtils.contains(values, null)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        if (isZero(value)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (isZero(value)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(toBigDecimal(value));
        }
        return result;
    }

    /**
     *
     * @since 4.0.0
     */
    public static BigDecimal mul(final String... values) {
        if (ArrayUtils.isEmpty(values) || ArrayUtils.contains(values, null)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(values[0]);
        if (isZero(result)) {
            return BigDecimal.ZERO;
        }

        BigDecimal ele;
        for (int i = 1; i < values.length; i++) {
            ele = toBigDecimal(values[i]);
            if (isZero(ele)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(ele);
        }

        return result;
    }

    // endregion

    // region ----- div

    /**
     *
     * @since 3.1.0
     */
    public static BigDecimal div(final Number v1, final Number v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    public static BigDecimal div(final String v1, final String v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     *
     * @since 3.1.0
     */
    public static BigDecimal div(final Number v1, final Number v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal div(final String v1, final String v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal div(final String v1, final String v2, final int scale, final RoundingMode roundingMode) {
        return div(toBigDecimal(v1), toBigDecimal(v2), scale, roundingMode);
    }

    /**
     *
     * @since 3.1.0
     */
    public static BigDecimal div(final Number v1, final Number v2, int scale, final RoundingMode roundingMode) {
        if (null == v1 || isZero(v1)) {
            // https://gitee.com/dromara/hutool/issues/I6UZYU
            return BigDecimal.ZERO;
        }

        if (scale < 0) {
            scale = -scale;
        }
        return toBigDecimal(v1).divide(toBigDecimal(v2), scale, roundingMode);
    }

    /**
     *
     * @since 5.3.3
     */
    public static int ceilDiv(final int v1, final int v2) {
        return (int) Math.ceil((double) v1 / v2);
    }

    // endregion

    // region ----- round

    public static BigDecimal round(final double v, final int scale) {
        return round(v, scale, RoundingMode.HALF_UP);
    }

    public static String roundStr(final double v, final int scale) {
        return round(v, scale).toPlainString();
    }

    /**
     *
     * @since 4.1.0
     */
    public static BigDecimal round(final BigDecimal number, final int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     *
     * @since 3.2.2
     */
    public static String roundStr(final String numberStr, final int scale) {
        return roundStr(numberStr, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(final double v, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(v), scale, roundingMode);
    }

    /**
     *
     * @since 3.2.2
     */
    public static String roundStr(final double v, final int scale, final RoundingMode roundingMode) {
        return round(v, scale, roundingMode).toPlainString();
    }

    public static BigDecimal round(BigDecimal number, int scale, RoundingMode roundingMode) {
        if (null == number) {
            number = BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = 0;
        }
        if (null == roundingMode) {
            roundingMode = RoundingMode.HALF_UP;
        }

        return number.setScale(scale, roundingMode);
    }

    /**
     *
     * @since 3.2.2
     */
    public static String roundStr(final String numberStr, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(numberStr), scale, roundingMode).toPlainString();
    }

    /**
     *
     *
     * <pre>
     * </pre>
     *
     * @since 4.1.0
     */
    public static BigDecimal roundHalfEven(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.HALF_EVEN);
    }

    /**
     *
     * @since 4.1.0
     */
    public static BigDecimal roundDown(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.DOWN);
    }

    // endregion

    // region ----- decimalFormat

    /**
     *
     * <ul>
     * </ul>
     */
    public static String format(final String pattern, final double value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     *
     * <ul>
     * </ul>
     *
     * @since 3.0.5
     */
    public static String format(final String pattern, final long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     *
     * <ul>
     * </ul>
     *
     * @since 5.1.6
     */
    public static String format(final String pattern, final Object value) {
        return format(pattern, value, null);
    }

    /**
     *
     * <ul>
     * </ul>
     *
     * @since 5.6.5
     */
    public static String format(final String pattern, final Object value, final RoundingMode roundingMode) {
        if (value instanceof Number) {}
        final DecimalFormat decimalFormat = new DecimalFormat(pattern);
        if (null != roundingMode) {
            decimalFormat.setRoundingMode(roundingMode);
        }
        return decimalFormat.format(value);
    }

    /**
     *
     * @since 3.0.9
     */
    public static String formatMoney(final double value) {
        return format(",##0.00", value);
    }

    /**
     *
     * @since 3.2.3
     */
    public static String formatPercent(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     *
     * @since 6.0.0
     */
    public static String formatThousands(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    // endregion

    // region ----- range

    public static int[] range(final int stopIncluded) {
        return range(0, stopIncluded, 1);
    }

    public static int[] range(final int startInclude, final int stopIncluded) {
        return range(startInclude, stopIncluded, 1);
    }

    public static int[] range(int startInclude, int stopIncluded, int step) {
        if (startInclude > stopIncluded) {
            final int tmp = startInclude;
            startInclude = stopIncluded;
            stopIncluded = tmp;
        }

        if (step <= 0) {
            step = 1;
        }

        final int deviation = stopIncluded + 1 - startInclude;
        int length = deviation / step;
        if (deviation % step != 0) {
            length += 1;
        }
        final int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = startInclude;
            startInclude += step;
        }
        return range;
    }

    public static Collection<Integer> appendRange(final int start, final int stop, final Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    public static Collection<Integer> appendRange(
            final int startInclude, final int stopInclude, int step, final Collection<Integer> values) {
        if (startInclude < stopInclude) {
            step = Math.abs(step);
        } else if (startInclude > stopInclude) {
            step = -Math.abs(step);
        } else { // start == end
            values.add(startInclude);
            return values;
        }

        for (int i = startInclude; (step > 0) ? i <= stopInclude : i >= stopInclude; i += step) {
            values.add(i);
        }
        return values;
    }

    // endregion

    // -------------------------------------------------------------------------------------------
    // others

    public static String getBinaryStr(final Number number) {
        if (number instanceof Long) {
            return Long.toBinaryString((Long) number);
        } else if (number instanceof Integer) {
            return Integer.toBinaryString((Integer) number);
        } else {
            return Long.toBinaryString(number.longValue());
        }
    }

    public static int binaryToInt(final String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    public static long binaryToLong(final String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    // region ----- equals

    /**
     *
     * <ul>
     * </ul>
     *
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(final Number number1, final Number number2) {
        if (number1 instanceof BigDecimal && number2 instanceof BigDecimal) {

            return ((BigDecimal) number1).compareTo((BigDecimal) number2) == 0;
        }
        return Objects.equals(number1, number2);
    }

    // endregion

    // region ----- toStr

    /**
     *
     * @since 3.0.9
     */
    public static String toStr(final Number number, final String defaultValue) {
        return (null == number) ? defaultValue : toStr(number);
    }

    public static String toStr(final Number number) {
        return toStr(number, true);
    }

    public static String toStr(final Number number, final boolean isStripTrailingZeros) {

        if (number instanceof BigDecimal) {
            return toStr((BigDecimal) number, isStripTrailingZeros);
        }

        String string = number.toString();
        if (isStripTrailingZeros) {
            if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
                while (string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }
                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }
        }
        return string;
    }

    /**
     *
     * @since 5.4.6
     */
    public static String toStr(final BigDecimal bigDecimal) {
        return toStr(bigDecimal, true);
    }

    /**
     *
     * @since 5.4.6
     */
    public static String toStr(BigDecimal bigDecimal, final boolean isStripTrailingZeros) {
        if (isStripTrailingZeros) {
            bigDecimal = bigDecimal.stripTrailingZeros();
        }
        return bigDecimal.toPlainString();
    }

    // endregion

    /**
     *
     * @since 4.0.9
     */
    public static BigDecimal toBigDecimal(final Number number) {
        if (null == number) {
            return BigDecimal.ZERO;
        }

        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof Long) {
            return new BigDecimal((Long) number);
        } else if (number instanceof Integer) {
            return new BigDecimal((Integer) number);
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        }

        return new BigDecimal(number.toString());
    }

    public static BigDecimal toBigDecimal(final String numberStr) throws IllegalArgumentException {

        try {
            return new BigDecimal(numberStr);
        } catch (final Exception ignore) {

        }

        return toBigDecimal(numberStr);
    }

    /**
     *
     * @since 5.4.5
     */
    public static BigInteger toBigInteger(final Number number) {

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof Long) {
            return BigInteger.valueOf((Long) number);
        }

        return toBigInteger(number.longValue());
    }

    /**
     *
     * @since 5.4.5
     */
    public static BigInteger toBigInteger(final String numberStr) {

        try {
            return new BigInteger(numberStr);
        } catch (final Exception ignore) {

        }

        return new BigInteger(numberStr);
    }

    /**
     *
     * <pre>
     * </pre>
     *
     * @since 3.0.6
     */
    public static int count(final int total, final int pageSize) {

        return (total + pageSize - 1) / pageSize;
    }

    /**
     *
     * @since 3.1.2
     */
    public static int zeroToOne(final int value) {
        return 0 == value ? 1 : value;
    }

    // region nullToZero

    public static int nullToZero(final Integer number) {
        return number == null ? 0 : number;
    }

    public static long nullToZero(final Long number) {
        return number == null ? 0L : number;
    }

    public static double nullToZero(final Double number) {
        return number == null ? 0.0 : number;
    }

    public static float nullToZero(final Float number) {
        return number == null ? 0.0f : number;
    }

    public static short nullToZero(final Short number) {
        return number == null ? (short) 0 : number;
    }

    public static byte nullToZero(final Byte number) {
        return number == null ? (byte) 0 : number;
    }

    public static BigInteger nullToZero(final BigInteger number) {
        return number != null ? number : BigInteger.ZERO;
    }

    /**
     *
     * @since 3.0.9
     */
    public static BigDecimal nullToZero(final BigDecimal decimal) {
        return decimal != null ? decimal : BigDecimal.ZERO;
    }

    // endregion

    /**
     *
     * @since 4.0.7
     */
    public static boolean isBeside(final long number1, final long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     *
     * @since 4.0.7
     */
    public static boolean isBeside(final int number1, final int number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     *
     * @since 4.0.7
     */
    public static int partValue(final int total, final int partCount) {
        return partValue(total, partCount, true);
    }

    /**
     *
     * @since 4.0.7
     */
    public static int partValue(final int total, final int partCount, final boolean isPlusOneWhenHasRem) {
        int partValue = total / partCount;
        if (isPlusOneWhenHasRem && total % partCount > 0) {
            partValue++;
        }
        return partValue;
    }

    /**
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(final Number number, final int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(final BigDecimal number, final int n) {
        return pow(number, n, 2, RoundingMode.HALF_UP);
    }

    /**
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(
            final BigDecimal number, final int n, final int scale, final RoundingMode roundingMode) {
        if (n < 0) {

            return BigDecimal.ONE.divide(pow(number, -n), scale, roundingMode);
        }
        return number.pow(n);
    }

    public static boolean isPowerOfTwo(final long n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    public static boolean isZero(final Number n) {
        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long) {
            return 0L == n.longValue();
        } else if (n instanceof BigInteger) {
            return equals(BigInteger.ZERO, n);
        } else if (n instanceof Float) {
            return 0f == n.floatValue();
        } else if (n instanceof Double) {
            return 0d == n.doubleValue();
        }
        return equals(toBigDecimal(n), BigDecimal.ZERO);
    }
}
