/*
 * #%L
 * LoadUp Common Context
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
package io.github.loadup.commons.util.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.enums.CurrencyEnum;
import io.github.loadup.commons.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class MoneyTest {
    @Test
    void retainsMinorAmountAndDerivesCurrencyCode() {
        Money value = Money.ofMinor(1234, CurrencyEnum.CNY);
        assertThat(value.getCent()).isEqualTo(1234);
        assertThat(value.getCurrency()).isEqualTo(Currency.getInstance("CNY"));
        assertThat(value.getCurrencyValue()).isEqualTo("CNY");
        assertThat(value.toMajor()).isEqualByComparingTo("12.34");
        assertThat(value).isEqualTo(new Money(1234L, "CNY"));
        assertThat(value.hashCode()).isEqualTo(new Money(1234L, "CNY").hashCode());
        assertThat(value).isNotEqualTo(Money.ofMinor(1234, "USD"));
    }

    @Test
    void handlesZeroTwoThreeAndFourFractionDigits() {
        assertThat(MoneyUtil.fromMajor(new BigDecimal("123"), "JPY").getCent()).isEqualTo(123);
        assertThat(MoneyUtil.fromMajor(new BigDecimal("1.23"), "CNY").getCent()).isEqualTo(123);
        assertThat(MoneyUtil.fromMajor(new BigDecimal("1.234"), "KWD").getCent())
                .isEqualTo(1234);
        assertThat(MoneyUtil.fromMajor(new BigDecimal("1.2345"), "CLF").getCent())
                .isEqualTo(12345);
        assertThat(Money.ofMinor(1234, "KWD").toMajor()).isEqualByComparingTo("1.234");
    }

    @Test
    void conversionRequiresExactAmountUnlessRoundingIsChosen() {
        assertThatThrownBy(() -> MoneyUtil.fromMajor(new BigDecimal("1.235"), "CNY"))
                .isInstanceOf(ArithmeticException.class);
        assertThat(MoneyUtil.fromMajor(new BigDecimal("1.235"), CurrencyEnum.CNY, RoundingMode.HALF_UP)
                        .getCent())
                .isEqualTo(124);
        assertThat(MoneyUtil.fromMajor(new BigDecimal("-1.235"), "CNY", RoundingMode.HALF_UP)
                        .getCent())
                .isEqualTo(-124);
        assertThatThrownBy(() -> MoneyUtil.fromMajor(new BigDecimal("92233720368547758.08"), "CNY"))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void rejectsPseudoCurrenciesAndMissingFields() {
        assertThatThrownBy(() -> Money.ofMinor(1, "XXX")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.ofMinor(1, "XAU")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money((Long) null, "CNY")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Money.ofMinor(1, "NOT_A_CODE")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void performsImmutableSignedArithmetic() {
        Money left = Money.ofMinor(100, "CNY");
        Money right = Money.ofMinor(40, "CNY");
        assertThat(MoneyUtil.add(left, right).getCent()).isEqualTo(140);
        assertThat(MoneyUtil.subtract(right, left).getCent()).isEqualTo(-60);
        assertThat(MoneyUtil.negate(right).getCent()).isEqualTo(-40);
        assertThat(MoneyUtil.abs(Money.ofMinor(-40, "CNY"))).isEqualTo(right);
        assertThat(MoneyUtil.multiply(right, 3).getCent()).isEqualTo(120);
        assertThat(left.getCent()).isEqualTo(100);
    }

    @Test
    void rejectsMixedCurrencyArithmeticAndComparison() {
        Money cny = Money.ofMinor(1, "CNY");
        Money usd = Money.ofMinor(1, "USD");
        assertThatThrownBy(() -> MoneyUtil.add(cny, usd)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoneyUtil.subtract(cny, usd)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cny.compareTo(usd)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoneyUtil.sum(List.of(cny, usd), cny.getCurrency()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detectsOverflowInEveryIntegralOperation() {
        Money max = Money.ofMinor(Long.MAX_VALUE, "CNY");
        Money min = Money.ofMinor(Long.MIN_VALUE, "CNY");
        Money one = Money.ofMinor(1, "CNY");
        assertThatThrownBy(() -> MoneyUtil.add(max, one)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.subtract(min, one)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.negate(min)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.abs(min)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.multiply(max, 2)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void decimalOperationsRoundOnlyAtTheFinalMinorUnit() {
        Money amount = Money.ofMinor(101, "CNY");
        assertThat(MoneyUtil.multiply(amount, new BigDecimal("0.5"), RoundingMode.HALF_UP)
                        .getCent())
                .isEqualTo(51);
        assertThat(MoneyUtil.divide(amount, new BigDecimal("2"), RoundingMode.DOWN)
                        .getCent())
                .isEqualTo(50);
        assertThatThrownBy(() -> MoneyUtil.multiply(amount, new BigDecimal("0.5"), RoundingMode.UNNECESSARY))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.divide(amount, BigDecimal.ZERO, RoundingMode.HALF_UP))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MoneyUtil.divide(
                        Money.ofMinor(Long.MIN_VALUE, "CNY"), new BigDecimal("-1"), RoundingMode.UNNECESSARY))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void sumChecksTheFinalTotalWithoutOrderDependentIntermediateOverflow() {
        Money max = Money.ofMinor(Long.MAX_VALUE, "CNY");
        Money one = Money.ofMinor(1, "CNY");
        assertThat(MoneyUtil.sum(List.of(max, one, Money.ofMinor(-1, "CNY")), max.getCurrency()))
                .isEqualTo(max);
        assertThat(MoneyUtil.sum(List.of(), max.getCurrency()).getCent()).isZero();
        assertThatThrownBy(() -> MoneyUtil.sum(List.of(max, one), max.getCurrency()))
                .isInstanceOf(ArithmeticException.class);
        assertThat(MoneyUtil.min(max, one)).isEqualTo(one);
        assertThat(MoneyUtil.max(max, one)).isEqualTo(max);
    }

    @Test
    void formattingDoesNotUseTheMachineDefaultLocaleOrLosePrecision() {
        Money cny = Money.ofMinor(1234, "CNY");
        assertThat(MoneyFormatter.format(cny)).isEqualTo("CNY 12.34");
        assertThat(MoneyFormatter.formatSymbol(cny)).isEqualTo("￥12.34");
        assertThat(MoneyFormatter.formatAmount(Money.ofMinor(1234, "JPY"))).isEqualTo("1234");
        assertThat(MoneyFormatter.formatAmount(Money.ofMinor(1234, "KWD"))).isEqualTo("1.234");
        assertThat(MoneyFormatter.formatAmount(Money.ofMinor(Long.MAX_VALUE, "CNY")))
                .isEqualTo("92233720368547758.07");
        assertThat(MoneyFormatter.format(Money.ofMinor(1234, "USD"), Locale.US)).isEqualTo("$12.34");
        assertThat(MoneyFormatter.format(Money.ofMinor(1234, "JPY"), Locale.US)).isEqualTo("¥1,234");
    }

    @Test
    void catalogMatchesJdkCodesAndPreservesNumericLeadingZerosAndRequestedSymbols() {
        var codes = java.util.Arrays.stream(CurrencyEnum.values())
                .map(CurrencyEnum::getCode)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(codes)
                .isEqualTo(Currency.availableCurrencies()
                        .map(Currency::getCurrencyCode)
                        .collect(java.util.stream.Collectors.toSet()));
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            assertThat(currency.getNumericCode())
                    .isEqualTo(currency.toCurrency().getNumericCodeAsString());
        }
        assertThat(CurrencyEnum.AFN.getNumericCode()).isEqualTo("971");
        assertThat(CurrencyEnum.ALL.getNumericCode()).isEqualTo("008");
        assertThat(CurrencyEnum.CNY.getSymbol()).isEqualTo("￥");
        assertThat(CurrencyEnum.USD.getSymbol()).isEqualTo("US $");
        assertThat(CurrencyEnum.GBP.getSymbol()).isEqualTo("￡");
    }

    @Test
    void jackson3RoundTripsOnlyCanonicalMinorAmountAndCurrencyCode() {
        var mapper = JsonMapper.builder().build();
        Money value = Money.ofMinor(Long.MAX_VALUE, CurrencyEnum.CNY);
        String json = mapper.writeValueAsString(value);
        var node = mapper.readTree(json);
        assertThat(node.size()).isEqualTo(2);
        assertThat(node.get("cent").asLong()).isEqualTo(Long.MAX_VALUE);
        assertThat(node.get("currencyValue").asString()).isEqualTo("CNY");
        assertThat(mapper.readValue(json, Money.class)).isEqualTo(value);
        assertThatThrownBy(() -> mapper.readValue("{\"currencyValue\":\"CNY\"}", Money.class))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> mapper.readValue("{\"cent\":1}", Money.class)).isInstanceOf(RuntimeException.class);
    }
}
