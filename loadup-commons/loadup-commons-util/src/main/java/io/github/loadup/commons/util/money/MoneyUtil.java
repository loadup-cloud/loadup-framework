/*
 * #%L
 * LoadUp Common Money
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

import io.github.loadup.commons.enums.CurrencyEnum;
import io.github.loadup.commons.money.Money;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Currency;
import java.util.Objects;

/** Exact minor-unit arithmetic. Fractional results always require an explicit rounding policy. */
public final class MoneyUtil {
    private MoneyUtil() {}

    public static Money fromMajor(BigDecimal amount, Currency currency) {
        return fromMajor(amount, currency, RoundingMode.UNNECESSARY);
    }

    public static Money fromMajor(BigDecimal amount, String currencyCode) {
        return fromMajor(amount, Currency.getInstance(currencyCode));
    }

    public static Money fromMajor(BigDecimal amount, CurrencyEnum currency) {
        return fromMajor(amount, Objects.requireNonNull(currency, "currency").toCurrency());
    }

    public static Money fromMajor(BigDecimal amount, String currencyCode, RoundingMode roundingMode) {
        return fromMajor(amount, Currency.getInstance(currencyCode), roundingMode);
    }

    public static Money fromMajor(BigDecimal amount, CurrencyEnum currency, RoundingMode roundingMode) {
        return fromMajor(amount, Objects.requireNonNull(currency, "currency").toCurrency(), roundingMode);
    }

    public static Money fromMajor(BigDecimal amount, Currency currency, RoundingMode roundingMode) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(roundingMode, "roundingMode");
        Money zero = Money.ofMinor(0, currency);
        long minor = amount.movePointRight(zero.getFractionDigits())
                .setScale(0, roundingMode)
                .longValueExact();
        return Money.ofMinor(minor, currency);
    }

    public static Money add(Money left, Money right) {
        sameCurrency(left, right);
        return Money.ofMinor(Math.addExact(left.getCent(), right.getCent()), left.getCurrency());
    }

    public static Money subtract(Money left, Money right) {
        sameCurrency(left, right);
        return Money.ofMinor(Math.subtractExact(left.getCent(), right.getCent()), left.getCurrency());
    }

    public static Money negate(Money money) {
        Objects.requireNonNull(money, "money");
        return Money.ofMinor(Math.negateExact(money.getCent()), money.getCurrency());
    }

    public static Money abs(Money money) {
        Objects.requireNonNull(money, "money");
        return money.getCent() < 0 ? negate(money) : money;
    }

    public static Money multiply(Money money, long factor) {
        Objects.requireNonNull(money, "money");
        return Money.ofMinor(Math.multiplyExact(money.getCent(), factor), money.getCurrency());
    }

    public static Money multiply(Money money, BigDecimal factor, RoundingMode roundingMode) {
        Objects.requireNonNull(money, "money");
        Objects.requireNonNull(factor, "factor");
        Objects.requireNonNull(roundingMode, "roundingMode");
        long result = BigDecimal.valueOf(money.getCent())
                .multiply(factor)
                .setScale(0, roundingMode)
                .longValueExact();
        return Money.ofMinor(result, money.getCurrency());
    }

    public static Money divide(Money money, BigDecimal divisor, RoundingMode roundingMode) {
        Objects.requireNonNull(money, "money");
        Objects.requireNonNull(divisor, "divisor");
        Objects.requireNonNull(roundingMode, "roundingMode");
        long result = BigDecimal.valueOf(money.getCent())
                .divide(divisor, 0, roundingMode)
                .longValueExact();
        return Money.ofMinor(result, money.getCurrency());
    }

    public static Money sum(Collection<Money> amounts, Currency currency) {
        Objects.requireNonNull(amounts, "amounts");
        Money zero = Money.ofMinor(0, currency);
        BigInteger total = BigInteger.ZERO;
        for (Money amount : amounts) {
            sameCurrency(zero, amount);
            total = total.add(BigInteger.valueOf(amount.getCent()));
        }
        return Money.ofMinor(total.longValueExact(), currency);
    }

    public static Money min(Money left, Money right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    public static Money max(Money left, Money right) {
        return left.compareTo(right) >= 0 ? left : right;
    }

    private static void sameCurrency(Money left, Money right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        if (!left.getCurrency().equals(right.getCurrency()))
            throw new IllegalArgumentException("Different currencies cannot be combined");
    }
}
