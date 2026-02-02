package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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

import java.math.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Objects;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.StringUtils;

/**
 * 数字工具类<br>
 * 对于精确值计算应该使用 {@link BigDecimal}<br>
 * JDK7中<strong>BigDecimal(double val)</strong>构造方法的结果有一定的不可预知性，例如：
 *
 * <pre>
 * new BigDecimal(0.1)和 BigDecimal.valueOf(0.1)
 * </pre>
 *
 * <p>表示的不是<strong>0.1</strong>而是<strong>0.1000000000000000055511151231257827021181583404541015625</strong>
 *
 * <p>这是因为0.1无法准确的表示为double。因此应该使用<strong>new BigDecimal(String)</strong>。 相关介绍：
 *
 * <ul>
 *   <li><a
 *       href="https://github.com/venusdrogon/feilong-core/wiki/one-jdk7-bug-thinking">one-jdk7-bug-thinking</a>
 * </ul>
 *
 * @author Looly
 */
public class NumberUtil {

    /** 默认除法运算精度 */
    private static final int DEFAULT_DIV_SCALE = 10;

    // region ----- add

    /**
     * 提供精确的加法运算<br>
     * 如果传入多个值为null或者空，则返回0
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
     * 提供精确的加法运算<br>
     * 如果传入多个值为null或者空，则返回
     *
     * <p>需要注意的是，在不同Locale下，数字的表示形式也是不同的，例如：<br>
     * 德国、荷兰、比利时、丹麦、意大利、罗马尼亚和欧洲大多地区使用`,`区分小数<br>
     * 也就是说，在这些国家地区，1.20表示120，而非1.2。
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
     * 提供精确的减法运算<br>
     * 如果传入多个值为null或者空，则返回0
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
     * 提供精确的减法运算<br>
     * 如果传入多个值为null或者空，则返回0
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
     * 提供精确的乘法运算<br>
     * 如果传入多个值为null或者空，则返回0
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
     * 提供精确的乘法运算<br>
     * 如果传入多个值为null或者空，则返回0
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
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @since 3.1.0
     */
    public static BigDecimal div(final Number v1, final Number v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /** 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入 */
    public static BigDecimal div(final String v1, final String v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @since 3.1.0
     */
    public static BigDecimal div(final Number v1, final Number v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /** 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入 */
    public static BigDecimal div(final String v1, final String v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /** 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度 */
    public static BigDecimal div(final String v1, final String v2, final int scale, final RoundingMode roundingMode) {
        return div(toBigDecimal(v1), toBigDecimal(v2), scale, roundingMode);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
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
     * 补充Math.ceilDiv() JDK8中添加了和 {@link Math#floorDiv(int, int)} 但却没有ceilDiv()
     *
     * @since 5.3.3
     */
    public static int ceilDiv(final int v1, final int v2) {
        return (int) Math.ceil((double) v1 / v2);
    }

    // endregion

    // region ----- round

    /**
     * 保留固定位数小数<br>
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}<br>
     * 例如保留2位小数：123.456789 =》 123.46
     */
    public static BigDecimal round(final double v, final int scale) {
        return round(v, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数<br>
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}<br>
     * 例如保留2位小数：123.456789 =》 123.46
     */
    public static String roundStr(final double v, final int scale) {
        return round(v, scale).toPlainString();
    }

    /**
     * 保留固定位数小数<br>
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}<br>
     * 例如保留2位小数：123.456789 =》 123.46
     *
     * @since 4.1.0
     */
    public static BigDecimal round(final BigDecimal number, final int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数<br>
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}<br>
     * 例如保留2位小数：123.456789 =》 123.46
     *
     * @since 3.2.2
     */
    public static String roundStr(final String numberStr, final int scale) {
        return roundStr(numberStr, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     */
    public static BigDecimal round(final double v, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(v), scale, roundingMode);
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @since 3.2.2
     */
    public static String roundStr(final double v, final int scale, final RoundingMode roundingMode) {
        return round(v, scale, roundingMode).toPlainString();
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     */
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
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @since 3.2.2
     */
    public static String roundStr(final String numberStr, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(numberStr), scale, roundingMode).toPlainString();
    }

    /**
     * 四舍六入五成双计算法
     *
     * <p>四舍六入五成双是一种比较精确比较科学的计数保留法，是一种数字修约规则。
     *
     * <pre>
     * 算法规则:
     * 四舍六入五考虑，
     * 五后非零就进一，
     * 五后皆零看奇偶，
     * 五前为偶应舍去，
     * 五前为奇要进一。
     * </pre>
     *
     * @since 4.1.0
     */
    public static BigDecimal roundHalfEven(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.HALF_EVEN);
    }

    /**
     * 保留固定小数位数，舍去多余位数
     *
     * @since 4.1.0
     */
    public static BigDecimal roundDown(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.DOWN);
    }

    // endregion

    // region ----- decimalFormat

    /**
     * 格式化double<br>
     * 对 {@link DecimalFormat} 做封装<br>
     *
     * <ul>
     *   <li>0 =》 取一位整数
     *   <li>0.00 =》 取一位整数和两位小数
     *   <li>00.000 =》 取两位整数和三位小数
     *   <li># =》 取所有整数部分
     *   <li>#.##% =》 以百分比方式计数，并取两位小数
     *   <li>#.#####E0 =》 显示为科学计数法，并取五位小数
     *   <li>,### =》 每三位以逗号进行分隔，例如：299,792,458
     *   <li>光速大小为每秒,###米 =》 将格式嵌入文本
     * </ul>
     */
    public static String format(final String pattern, final double value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double<br>
     * 对 {@link DecimalFormat} 做封装<br>
     *
     * <ul>
     *   <li>0 =》 取一位整数
     *   <li>0.00 =》 取一位整数和两位小数
     *   <li>00.000 =》 取两位整数和三位小数
     *   <li># =》 取所有整数部分
     *   <li>#.##% =》 以百分比方式计数，并取两位小数
     *   <li>#.#####E0 =》 显示为科学计数法，并取五位小数
     *   <li>,### =》 每三位以逗号进行分隔，例如：299,792,458
     *   <li>光速大小为每秒,###米 =》 将格式嵌入文本
     * </ul>
     *
     * @since 3.0.5
     */
    public static String format(final String pattern, final long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double<br>
     * 对 {@link DecimalFormat} 做封装<br>
     *
     * <ul>
     *   <li>0 =》 取一位整数
     *   <li>0.00 =》 取一位整数和两位小数
     *   <li>00.000 =》 取两位整数和三位小数
     *   <li># =》 取所有整数部分
     *   <li>#.##% =》 以百分比方式计数，并取两位小数
     *   <li>#.#####E0 =》 显示为科学计数法，并取五位小数
     *   <li>,### =》 每三位以逗号进行分隔，例如：299,792,458
     *   <li>光速大小为每秒,###米 =》 将格式嵌入文本
     * </ul>
     *
     * @since 5.1.6
     */
    public static String format(final String pattern, final Object value) {
        return format(pattern, value, null);
    }

    /**
     * 格式化double<br>
     * 对 {@link DecimalFormat} 做封装<br>
     *
     * <ul>
     *   <li>0 =》 取一位整数
     *   <li>0.00 =》 取一位整数和两位小数
     *   <li>00.000 =》 取两位整数和三位小数
     *   <li># =》 取所有整数部分
     *   <li>#.##% =》 以百分比方式计数，并取两位小数
     *   <li>#.#####E0 =》 显示为科学计数法，并取五位小数
     *   <li>,### =》 每三位以逗号进行分隔，例如：299,792,458
     *   <li>光速大小为每秒,###米 =》 将格式嵌入文本
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
     * 格式化金额输出，每三位用逗号分隔
     *
     * @since 3.0.9
     */
    public static String formatMoney(final double value) {
        return format(",##0.00", value);
    }

    /**
     * 格式化百分比，小数采用四舍五入方式
     *
     * @since 3.2.3
     */
    public static String formatPercent(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * 格式化千分位表示方式，小数采用四舍五入方式
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

    /** 生成一个从0开始的数字列表<br> */
    public static int[] range(final int stopIncluded) {
        return range(0, stopIncluded, 1);
    }

    /**
     * 生成一个数字列表<br>
     * 自动判定正序反序
     */
    public static int[] range(final int startInclude, final int stopIncluded) {
        return range(startInclude, stopIncluded, 1);
    }

    /**
     * 生成一个数字列表<br>
     * 自动判定正序反序
     */
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

    /** 将给定范围内的整数添加到已有集合中，步进为1 */
    public static Collection<Integer> appendRange(final int start, final int stop, final Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /** 将给定范围内的整数添加到已有集合中 */
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

    /** 获得数字对应的二进制字符串 */
    public static String getBinaryStr(final Number number) {
        if (number instanceof Long) {
            return Long.toBinaryString((Long) number);
        } else if (number instanceof Integer) {
            return Integer.toBinaryString((Integer) number);
        } else {
            return Long.toBinaryString(number.longValue());
        }
    }

    /** 二进制转int */
    public static int binaryToInt(final String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    /** 二进制转long */
    public static long binaryToLong(final String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    // region ----- equals

    /**
     * 比较数字值是否相等，相等返回{@code true}<br>
     * 需要注意的是{@link BigDecimal}需要特殊处理<br>
     * BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等，<br>
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * <ul>
     *   <li>如果用户提供两个Number都是{@link BigDecimal}，则通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等
     *   <li>其他情况调用{@link Number#equals(Object)}比较
     * </ul>
     *
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(final Number number1, final Number number2) {
        if (number1 instanceof BigDecimal && number2 instanceof BigDecimal) {
            // BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等
            return ((BigDecimal) number1).compareTo((BigDecimal) number2) == 0;
        }
        return Objects.equals(number1, number2);
    }

    // endregion

    // region ----- toStr

    /**
     * 数字转字符串<br>
     * 调用{@link Number#toString()}，并去除尾小数点儿后多余的0
     *
     * @since 3.0.9
     */
    public static String toStr(final Number number, final String defaultValue) {
        return (null == number) ? defaultValue : toStr(number);
    }

    /**
     * 数字转字符串<br>
     * 调用{@link Number#toString()}或 {@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     */
    public static String toStr(final Number number) {
        return toStr(number, true);
    }

    /**
     * 数字转字符串<br>
     * 调用{@link Number#toString()}或 {@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     */
    public static String toStr(final Number number, final boolean isStripTrailingZeros) {

        // BigDecimal单独处理，使用非科学计数法
        if (number instanceof BigDecimal) {
            return toStr((BigDecimal) number, isStripTrailingZeros);
        }

        // 去掉小数点儿后多余的0
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
     * {@link BigDecimal}数字转字符串<br>
     * 调用{@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     *
     * @since 5.4.6
     */
    public static String toStr(final BigDecimal bigDecimal) {
        return toStr(bigDecimal, true);
    }

    /**
     * {@link BigDecimal}数字转字符串<br>
     * 调用{@link BigDecimal#toPlainString()}，可选去除尾小数点儿后多余的0
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
     * 数字转{@link BigDecimal}<br>
     * Float、Double等有精度问题，转换为字符串后再转换<br>
     * null转换为0
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

        // Float、Double等有精度问题，转换为字符串后再转换
        return new BigDecimal(number.toString());
    }

    /**
     * 数字转{@link BigDecimal}<br>
     * null或""或空白符抛出{@link IllegalArgumentException}异常<br>
     * "NaN"转为{@link BigDecimal#ZERO}
     *
     * @throws IllegalArgumentException null或""或"NaN"或空白符抛出此异常
     */
    public static BigDecimal toBigDecimal(final String numberStr) throws IllegalArgumentException {
        // 统一规则，不再转换带有歧义的null、""和空格

        // issue#3241，优先调用构造解析
        try {
            return new BigDecimal(numberStr);
        } catch (final Exception ignore) {
            // 忽略解析错误
        }

        // 支持类似于 1,234.55 格式的数字
        return toBigDecimal(numberStr);
    }

    /**
     * 数字转{@link BigInteger}<br>
     * null或"NaN"转换为0
     *
     * @since 5.4.5
     */
    public static BigInteger toBigInteger(final Number number) {
        // 统一规则，不再转换带有歧义的null

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof Long) {
            return BigInteger.valueOf((Long) number);
        }

        return toBigInteger(number.longValue());
    }

    /**
     * 数字转{@link BigInteger}<br>
     * null或""或空白符转换为0
     *
     * @since 5.4.5
     */
    public static BigInteger toBigInteger(final String numberStr) {
        // 统一规则，不再转换带有歧义的null、""和空格

        try {
            return new BigInteger(numberStr);
        } catch (final Exception ignore) {
            // 忽略解析错误
        }

        return new BigInteger(numberStr);
    }

    /**
     * 计算等份个数
     *
     * <pre>
     *     (每份2)12   34  57
     *     (每份3)123  456 7
     *     (每份4)1234 567
     * </pre>
     *
     * @since 3.0.6
     */
    public static int count(final int total, final int pageSize) {
        // 因为总条数除以页大小的最大余数是页大小数-1，
        // 因此加一个最大余数，保证舍弃的余数与最大余数凑1.x，就是一旦有余数则+1页
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 如果给定值为0，返回1，否则返回原值
     *
     * @since 3.1.2
     */
    public static int zeroToOne(final int value) {
        return 0 == value ? 1 : value;
    }

    // region nullToZero

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static int nullToZero(final Integer number) {
        return number == null ? 0 : number;
    }

    /** 如果给定值为0，返回1，否则返回原值 */
    public static long nullToZero(final Long number) {
        return number == null ? 0L : number;
    }

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static double nullToZero(final Double number) {
        return number == null ? 0.0 : number;
    }

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static float nullToZero(final Float number) {
        return number == null ? 0.0f : number;
    }

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static short nullToZero(final Short number) {
        return number == null ? (short) 0 : number;
    }

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static byte nullToZero(final Byte number) {
        return number == null ? (byte) 0 : number;
    }

    /** 如果给定值为{@code null}，返回0，否则返回原值 */
    public static BigInteger nullToZero(final BigInteger number) {
        return ObjectUtils.defaultIfNull(number, BigInteger.ZERO);
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @since 3.0.9
     */
    public static BigDecimal nullToZero(final BigDecimal decimal) {
        return ObjectUtils.defaultIfNull(decimal, BigDecimal.ZERO);
    }

    // endregion

    /**
     * 判断两个数字是否相邻，例如1和2相邻，1和3不相邻<br>
     * 判断方法为做差取绝对值判断是否为1
     *
     * @since 4.0.7
     */
    public static boolean isBeside(final long number1, final long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 判断两个数字是否相邻，例如1和2相邻，1和3不相邻<br>
     * 判断方法为做差取绝对值判断是否为1
     *
     * @since 4.0.7
     */
    public static boolean isBeside(final int number1, final int number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 把给定的总数平均分成N份，返回每份的个数<br>
     * 当除以分数有余数时每份+1
     *
     * @since 4.0.7
     */
    public static int partValue(final int total, final int partCount) {
        return partValue(total, partCount, true);
    }

    /**
     * 把给定的总数平均分成N份，返回每份的个数<br>
     * 如果isPlusOneWhenHasRem为true，则当除以分数有余数时每份+1，否则丢弃余数部分
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
     * 提供精确的幂运算
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(final Number number, final int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     * 提供精确的幂运算<br>
     * 如果n为负数，则返回1/a的-n次方，默认四舍五入
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(final BigDecimal number, final int n) {
        return pow(number, n, 2, RoundingMode.HALF_UP);
    }

    /**
     * 提供精确的幂运算<br>
     * 如果n为负数，则返回1/a的-n次方，默认四舍五入
     *
     * @since 4.1.0
     */
    public static BigDecimal pow(
            final BigDecimal number, final int n, final int scale, final RoundingMode roundingMode) {
        if (n < 0) {
            // a的n次方，如果n为负数，则返回1/a的-n次方
            return BigDecimal.ONE.divide(pow(number, -n), scale, roundingMode);
        }
        return number.pow(n);
    }

    /** 判断一个整数是否是2的幂 */
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
