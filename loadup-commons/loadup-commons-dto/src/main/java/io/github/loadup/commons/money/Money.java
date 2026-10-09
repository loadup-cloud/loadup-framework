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
package io.github.loadup.commons.money;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loadup.commons.enums.CurrencyEnum;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/** Immutable signed amount in the currency's smallest decimal unit. */
public final class Money implements Comparable<Money> {
    private final long cent;
    private final Currency currency;
    private final String currencyValue;

    public Money(long cent, Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency");
        if (currency.getDefaultFractionDigits() < 0) {
            throw new IllegalArgumentException("Currency has no defined minor unit: " + currency.getCurrencyCode());
        }
        this.cent = cent;
        this.currencyValue = currency.getCurrencyCode();
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Money(
            @JsonProperty(value = "cent", required = true) Long cent,
            @JsonProperty(value = "currencyValue", required = true) String currencyValue) {
        this(
                Objects.requireNonNull(cent, "cent").longValue(),
                Currency.getInstance(Objects.requireNonNull(currencyValue, "currencyValue")));
    }

    public static Money ofMinor(long cent, Currency currency) {
        return new Money(cent, currency);
    }

    public static Money ofMinor(long cent, String currencyCode) {
        return new Money(cent, Currency.getInstance(currencyCode));
    }

    public static Money ofMinor(long cent, CurrencyEnum currency) {
        return new Money(cent, Objects.requireNonNull(currency, "currency").toCurrency());
    }

    public long getCent() {
        return cent;
    }

    @JsonIgnore
    public Currency getCurrency() {
        return currency;
    }

    public String getCurrencyValue() {
        return currencyValue;
    }

    @JsonIgnore
    public int getFractionDigits() {
        return currency.getDefaultFractionDigits();
    }

    public BigDecimal toMajor() {
        return BigDecimal.valueOf(cent, getFractionDigits());
    }

    @Override
    public int compareTo(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency))
            throw new IllegalArgumentException("Different currencies cannot be compared");
        return Long.compare(cent, other.cent);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Money money && cent == money.cent && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cent, currency);
    }

    @Override
    public String toString() {
        return currencyValue + " " + toMajor().toPlainString();
    }
}
