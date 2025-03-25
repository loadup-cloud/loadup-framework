package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-lang
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

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机工具类
 */
public class RandomUtil {

    /**
     * 用于随机选的数字
     */
    public static final String BASE_NUMBER = "0123456789";
    /**
     * 用于随机选的字符
     */
    public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 用于随机选的字符和数字（小写）
     */
    public static final String BASE_CHAR_NUMBER_LOWER = BASE_CHAR + BASE_NUMBER;
    /**
     * 用于随机选的字符和数字（包括大写和小写字母）
     */
    public static final String BASE_CHAR_NUMBER = BASE_CHAR.toUpperCase() + BASE_CHAR_NUMBER_LOWER;

    // region ----- get or create Random

    /**
     * 获取随机数生成器对象<br>
     * ThreadLocalRandom是JDK 7之后提供并发产生随机数，能够解决多个线程发生的竞争争夺。
     *
     * <p>
     * 注意：此方法返回的{@link ThreadLocalRandom}不可以在多线程环境下共享对象，否则有重复随机数问题。
     * 见：<a href="https://www.jianshu.com/p/89dfe990295c">https://www.jianshu.com/p/89dfe990295c</a>
     * </p>
     *
     * @since 3.1.2
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 创建{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     *
     * @since 4.6.5
     */
    public static SecureRandom createSecureRandom(final byte[] seed) {
        return (null == seed) ? new SecureRandom() : new SecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * <p>
     * 相关说明见：<a
     * href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @since 3.1.2
     */
    public static SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * <p>
     * 相关说明见：<a
     * href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @see #createSecureRandom(byte[])
     * @since 5.5.2
     */
    public static SecureRandom getSecureRandom(final byte[] seed) {
        return createSecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）,在Linux下噪声生成时可能造成较长时间停顿。<br>
     * see: <a href="http://ifeve.com/jvm-random-and-entropy-source/">http://ifeve.com/jvm-random-and-entropy-source/</a>
     *
     * <p>
     * 相关说明见：<a
     * href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @since 5.5.8
     */
    public static SecureRandom getSHA1PRNGRandom(final byte[] seed) {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        if (null != seed) {
            random.setSeed(seed);
        }
        return random;
    }

    /**
     * 获取algorithms/providers中提供的强安全随机生成器<br>
     * 注意：此方法可能造成阻塞或性能问题
     *
     * @since 5.7.12
     */
    public static SecureRandom getSecureRandomStrong() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取随机数产生器
     *
     * @see #getSecureRandom()
     * @see #getRandom()
     * @since 4.1.15
     */
    public static Random getRandom(final boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }
    // endregion

    /**
     * 获得随机Boolean值
     *
     * @since 4.5.9
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * 随机bytes
     */
    public static byte[] randomBytes(final int length) {
        return randomBytes(length, getRandom());
    }

    /**
     * 随机bytes
     *
     * @since 6.0.0
     */
    public static byte[] randomBytes(final int length, Random random) {
        if (null == random) {
            random = getRandom();
        }
        final byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * 随机汉字（'\u4E00'-'\u9FFF'）
     *
     * @since 5.7.15
     */
    public static char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

    // region ----- randomInt

    /**
     * 获得随机数int值
     *
     * @see Random#nextInt()
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @see Random#nextInt(int)
     */
    public static int randomInt(final int limitExclude) {
        return getRandom().nextInt(limitExclude);
    }

    /**
     * 获得指定范围内的随机数
     */
    public static int randomInt(final int minInclude, final int maxExclude) {
        return randomInt(minInclude, maxExclude, true, false);
    }

    /**
     * 获得指定范围内的随机数
     */
    public static int randomInt(int min, int max, final boolean includeMin, final boolean includeMax) {
        if (!includeMin) {
            min++;
        }
        if (includeMax) {
            max++;
        }
        return getRandom().nextInt(min, max);
    }

    /**
     * 创建指定长度的随机索引
     *
     * @since 5.2.1
     */
    public static int[] randomInts(final int length) {
        final int[] range = NumberUtil.range(length);
        for (int i = 0; i < length; i++) {
            final int random = randomInt(i, length);
            ArrayUtils.swap(range, i, random);
        }
        return range;
    }
    // endregion

    // region ----- randomLong

    /**
     * 获得随机数
     *
     * @see ThreadLocalRandom#nextLong()
     * @since 3.3.0
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @see ThreadLocalRandom#nextLong(long)
     */
    public static long randomLong(final long limitExclude) {
        return getRandom().nextLong(limitExclude);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @see ThreadLocalRandom#nextLong(long, long)
     * @since 3.3.0
     */
    public static long randomLong(final long minInclude, final long maxExclude) {
        return randomLong(minInclude, maxExclude, true, false);
    }

    /**
     * 获得指定范围内的随机数
     */
    public static long randomLong(long min, long max, final boolean includeMin, final boolean includeMax) {
        if (!includeMin) {
            min++;
        }
        if (includeMax) {
            max++;
        }
        return getRandom().nextLong(min, max);
    }
    // endregion

    // region ----- randomFloat

    /**
     * 获得随机数[0, 1)
     *
     * @see ThreadLocalRandom#nextFloat()
     */
    public static float randomFloat() {
        return getRandom().nextFloat();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     */
    public static float randomFloat(final float limitExclude) {
        return randomFloat(0, limitExclude);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @see ThreadLocalRandom#nextFloat()
     */
    public static float randomFloat(final float minInclude, final float maxExclude) {
        if (minInclude == maxExclude) {
            return minInclude;
        }

        return minInclude + ((maxExclude - minInclude) * getRandom().nextFloat());
    }
    // endregion

    // region ----- randomDouble

    /**
     * 获得指定范围内的随机数
     *
     * @see ThreadLocalRandom#nextDouble(double, double)
     * @since 3.3.0
     */
    public static double randomDouble(final double minInclude, final double maxExclude) {
        return getRandom().nextDouble(minInclude, maxExclude);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @since 4.0.8
     */
    public static double randomDouble(final double minInclude, final double maxExclude, final int scale,
                                      final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(minInclude, maxExclude), scale, roundingMode).doubleValue();
    }

    /**
     * 获得随机数[0, 1)
     *
     * @see ThreadLocalRandom#nextDouble()
     * @since 3.3.0
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * 获得指定范围内的随机数
     *
     * @since 4.0.8
     */
    public static double randomDouble(final int scale, final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @see ThreadLocalRandom#nextDouble(double)
     * @since 3.3.0
     */
    public static double randomDouble(final double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @since 4.0.8
     */
    public static double randomDouble(final double limit, final int scale, final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }
    // endregion

    // region ----- randomBigDecimal

    /**
     * 获得指定范围内的随机数[0, 1)
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal() {
        return NumberUtil.toBigDecimal(getRandom().nextDouble());
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal(final BigDecimal limitExclude) {
        return NumberUtil.toBigDecimal(getRandom().nextDouble(limitExclude.doubleValue()));
    }

    /**
     * 获得指定范围内的随机数
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal(final BigDecimal minInclude, final BigDecimal maxExclude) {
        return NumberUtil.toBigDecimal(getRandom().nextDouble(minInclude.doubleValue(), maxExclude.doubleValue()));
    }
    // endregion

    // region ----- randomEle

    /**
     * 随机获得列表中的元素
     */
    public static <T> T randomEle(final List<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * 随机获得列表中的元素
     */
    public static <T> T randomEle(final List<T> list, int limit) {
        if (list.size() < limit) {
            limit = list.size();
        }
        return list.get(randomInt(limit));
    }

    /**
     * 随机获得数组中的元素
     *
     * @since 3.3.0
     */
    public static <T> T randomEle(final T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * 随机获得数组中的元素
     *
     * @since 3.3.0
     */
    public static <T> T randomEle(final T[] array, int limit) {
        if (array.length < limit) {
            limit = array.length;
        }
        return array[randomInt(limit)];
    }

    /**
     * 随机获得列表中的一定量元素
     */
    public static <T> List<T> randomEles(final List<T> list, final int count) {
        final List<T> result = new ArrayList<>(count);
        final int limit = list.size();
        while (result.size() < count) {
            result.add(randomEle(list, limit));
        }

        return result;
    }

    /**
     * 生成从种子中获取随机数字
     *
     * @since 5.4.5
     */
    public static int[] randomPickInts(final int size, final int[] seed) {

        final int[] ranArr = new int[size];
        // 数量你可以自己定义。
        for (int i = 0; i < size; i++) {
            // 得到一个位置
            final int j = RandomUtil.randomInt(seed.length - i);
            // 得到那个位置的数值
            ranArr[i] = seed[j];
            // 将最后一个未用的数字放到这里
            seed[j] = seed[seed.length - 1 - i];
        }
        return ranArr;
    }

    // endregion

    // region ----- randomString

    /**
     * 获得一个随机的字符串（只包含数字和大小写字母）
     */
    public static String randomString(final int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母）
     */
    public static String randomStringLower(final int length) {
        return randomString(BASE_CHAR_NUMBER_LOWER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和大写字符）
     *
     * @since 4.0.13
     */
    public static String randomStringUpper(final int length) {
        return randomString(BASE_CHAR_NUMBER_LOWER, length).toUpperCase();
    }

    /**
     * 获得一个随机的字符串（只包含数字和字母） 并排除指定字符串
     */
    public static String randomStringWithoutStr(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER;
        baseStr = StringUtils.removeAll(baseStr, elemData);
        return randomString(baseStr, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母） 并排除指定字符串
     *
     * @since 5.8.28
     */
    public static String randomStringLowerWithoutStr(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER_LOWER;
        baseStr = StringUtils.removeAll(baseStr, elemData.toLowerCase());
        return randomString(baseStr, length);
    }

    /**
     * 获得一个只包含数字的字符串
     */
    public static String randomNumbers(final int length) {
        return randomString(BASE_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串
     */
    public static String randomString(final String baseString, int length) {
        if (StringUtils.isEmpty(baseString)) {
            return StringUtils.EMPTY;
        }
        if (length < 1) {
            length = 1;
        }

        final StringBuilder sb = new StringBuilder(length);
        final int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            final int number = randomInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }
    // endregion

    // region ---- randomChar

    /**
     * 随机数字，数字为0~9单个数字
     *
     * @since 3.1.2
     */
    public static char randomNumber() {
        return randomChar(BASE_NUMBER);
    }

    /**
     * 随机字母或数字，小写
     *
     * @since 3.1.2
     */
    public static char randomChar() {
        return randomChar(BASE_CHAR_NUMBER_LOWER);
    }

    /**
     * 随机字符
     *
     * @since 3.1.2
     */
    public static char randomChar(final String baseString) {
        return baseString.charAt(randomInt(baseString.length()));
    }
    // endregion

}
