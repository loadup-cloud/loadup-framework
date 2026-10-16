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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class NumberUtilTest {
    @Test
    void convertsDecimalStringsWithoutLosingPrecision() {
        assertThat(NumberUtil.toBigDecimal("12345678901234567890.00100"))
                .isEqualTo(new BigDecimal("12345678901234567890.00100"));
        assertThat(NumberUtil.toBigDecimal("-1.25e3")).isEqualByComparingTo("-1250");
    }

    @Test
    void rejectsInvalidNumbersWithoutRecursing() {
        assertThatThrownBy(() -> NumberUtil.toBigDecimal("invalid")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> NumberUtil.toBigDecimal("")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> NumberUtil.toBigDecimal((String) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> NumberUtil.toBigInteger("invalid")).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void convertsIntegralNumbersWithoutRecursing() {
        assertThat(NumberUtil.toBigInteger(Integer.valueOf(42))).isEqualTo(BigInteger.valueOf(42));
        assertThat(NumberUtil.toBigInteger(Double.valueOf(-42.9))).isEqualTo(BigInteger.valueOf(-42));
        assertThat(NumberUtil.toBigInteger("12345678901234567890")).isEqualTo(new BigInteger("12345678901234567890"));
    }
}
