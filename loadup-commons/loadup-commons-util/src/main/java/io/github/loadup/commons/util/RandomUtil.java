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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;

public class RandomUtil {

    public static final String BASE_NUMBER = "0123456789";

    public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyz";

    public static final String BASE_CHAR_NUMBER_LOWER = BASE_CHAR + BASE_NUMBER;

    public static final String BASE_CHAR_NUMBER = BASE_CHAR.toUpperCase() + BASE_CHAR_NUMBER_LOWER;

    // region ----- get or create Random

    /**
     *
     * href="https://www.jianshu.com/p/89dfe990295c">https://www.jianshu.com/p/89dfe990295c</a>
     *
     * @since 3.1.2
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     *
     * @since 4.6.5
     */
    public static SecureRandom createSecureRandom(final byte[] seed) {
        return (null == seed) ? new SecureRandom() : new SecureRandom(seed);
    }

    /**
     *
     * href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @since 3.1.2
     */
    public static SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     *
     * href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @see #createSecureRandom(byte[])
     * @since 5.5.2
     */
    public static SecureRandom getSecureRandom(final byte[] seed) {
        return createSecureRandom(seed);
    }

    /**
     * see: <a
     * href="http://ifeve.com/jvm-random-and-entropy-source/">http://ifeve.com/jvm-random-and-entropy-source/</a>
     *
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
     *
     * @since 4.5.9
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    public static byte[] randomBytes(final int length) {
        return randomBytes(length, getRandom());
    }

    /**
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
     *
     * @since 5.7.15
     */
    public static char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

    // region ----- randomInt

    /**
     *
     * @see Random#nextInt()
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     *
     * @see Random#nextInt(int)
     */
    public static int randomInt(final int limitExclude) {
        return getRandom().nextInt(limitExclude);
    }

    public static int randomInt(final int minInclude, final int maxExclude) {
        return randomInt(minInclude, maxExclude, true, false);
    }

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
     *
     * @see ThreadLocalRandom#nextLong()
     * @since 3.3.0
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     *
     * @see ThreadLocalRandom#nextLong(long)
     */
    public static long randomLong(final long limitExclude) {
        return getRandom().nextLong(limitExclude);
    }

    /**
     *
     * @see ThreadLocalRandom#nextLong(long, long)
     * @since 3.3.0
     */
    public static long randomLong(final long minInclude, final long maxExclude) {
        return randomLong(minInclude, maxExclude, true, false);
    }

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
     *
     * @see ThreadLocalRandom#nextFloat()
     */
    public static float randomFloat() {
        return getRandom().nextFloat();
    }

    public static float randomFloat(final float limitExclude) {
        return randomFloat(0, limitExclude);
    }

    /**
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
     *
     * @see ThreadLocalRandom#nextDouble(double, double)
     * @since 3.3.0
     */
    public static double randomDouble(final double minInclude, final double maxExclude) {
        return getRandom().nextDouble(minInclude, maxExclude);
    }

    /**
     *
     * @since 4.0.8
     */
    public static double randomDouble(
            final double minInclude, final double maxExclude, final int scale, final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(minInclude, maxExclude), scale, roundingMode)
                .doubleValue();
    }

    /**
     *
     * @see ThreadLocalRandom#nextDouble()
     * @since 3.3.0
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     *
     * @since 4.0.8
     */
    public static double randomDouble(final int scale, final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     *
     * @see ThreadLocalRandom#nextDouble(double)
     * @since 3.3.0
     */
    public static double randomDouble(final double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     *
     * @since 4.0.8
     */
    public static double randomDouble(final double limit, final int scale, final RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }

    // endregion

    // region ----- randomBigDecimal

    /**
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal() {
        return NumberUtil.toBigDecimal(getRandom().nextDouble());
    }

    /**
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal(final BigDecimal limitExclude) {
        return NumberUtil.toBigDecimal(getRandom().nextDouble(limitExclude.doubleValue()));
    }

    /**
     *
     * @since 4.0.9
     */
    public static BigDecimal randomBigDecimal(final BigDecimal minInclude, final BigDecimal maxExclude) {
        return NumberUtil.toBigDecimal(getRandom().nextDouble(minInclude.doubleValue(), maxExclude.doubleValue()));
    }

    // endregion

    // region ----- randomEle

    public static <T> T randomEle(final List<T> list) {
        return randomEle(list, list.size());
    }

    public static <T> T randomEle(final List<T> list, int limit) {
        if (list.size() < limit) {
            limit = list.size();
        }
        return list.get(randomInt(limit));
    }

    /**
     *
     * @since 3.3.0
     */
    public static <T> T randomEle(final T[] array) {
        return randomEle(array, array.length);
    }

    /**
     *
     * @since 3.3.0
     */
    public static <T> T randomEle(final T[] array, int limit) {
        if (array.length < limit) {
            limit = array.length;
        }
        return array[randomInt(limit)];
    }

    public static <T> List<T> randomEles(final List<T> list, final int count) {
        final List<T> result = new ArrayList<>(count);
        final int limit = list.size();
        while (result.size() < count) {
            result.add(randomEle(list, limit));
        }

        return result;
    }

    /**
     *
     * @since 5.4.5
     */
    public static int[] randomPickInts(final int size, final int[] seed) {

        final int[] ranArr = new int[size];

        for (int i = 0; i < size; i++) {

            final int j = RandomUtil.randomInt(seed.length - i);

            ranArr[i] = seed[j];

            seed[j] = seed[seed.length - 1 - i];
        }
        return ranArr;
    }

    // endregion

    // region ----- randomString

    public static String randomString(final int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    public static String randomStringLower(final int length) {
        return randomString(BASE_CHAR_NUMBER_LOWER, length);
    }

    /**
     *
     * @since 4.0.13
     */
    public static String randomStringUpper(final int length) {
        return randomString(BASE_CHAR_NUMBER_LOWER, length).toUpperCase();
    }

    public static String randomStringWithoutStr(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER;
        for (char c : elemData.toCharArray()) {
            baseStr = baseStr.replace(String.valueOf(c), "");
        }
        return randomString(baseStr, length);
    }

    /**
     *
     * @since 5.8.28
     */
    public static String randomStringLowerWithoutStr(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER_LOWER;
        for (char c : elemData.toLowerCase().toCharArray()) {
            baseStr = baseStr.replace(String.valueOf(c), "");
        }
        return randomString(baseStr, length);
    }

    public static String randomNumbers(final int length) {
        return randomString(BASE_NUMBER, length);
    }

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
     *
     * @since 3.1.2
     */
    public static char randomNumber() {
        return randomChar(BASE_NUMBER);
    }

    /**
     *
     * @since 3.1.2
     */
    public static char randomChar() {
        return randomChar(BASE_CHAR_NUMBER_LOWER);
    }

    /**
     *
     * @since 3.1.2
     */
    public static char randomChar(final String baseString) {
        return baseString.charAt(randomInt(baseString.length()));
    }
    // endregion

}
