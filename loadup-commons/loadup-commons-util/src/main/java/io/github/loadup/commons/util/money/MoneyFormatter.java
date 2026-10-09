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
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/** Stateless formatting; localized formatters are created per call and never shared across threads. */
public final class MoneyFormatter {
    private MoneyFormatter() {}

    public static String format(Money money) {
        Objects.requireNonNull(money, "money");
        return money.getCurrencyValue() + " " + formatAmount(money);
    }

    public static String formatAmount(Money money) {
        return Objects.requireNonNull(money, "money").toMajor().toPlainString();
    }

    public static String formatSymbol(Money money) {
        Objects.requireNonNull(money, "money");
        String symbol;
        try {
            symbol = CurrencyEnum.fromCode(money.getCurrencyValue()).getSymbol();
        } catch (IllegalArgumentException e) {
            symbol = money.getCurrencyValue();
        }
        return symbol + formatAmount(money);
    }

    public static String format(Money money, Locale locale) {
        Objects.requireNonNull(money, "money");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Objects.requireNonNull(locale, "locale"));
        formatter.setCurrency(money.getCurrency());
        formatter.setMinimumFractionDigits(money.getFractionDigits());
        formatter.setMaximumFractionDigits(money.getFractionDigits());
        formatter.setRoundingMode(RoundingMode.UNNECESSARY);
        return formatter.format(money.toMajor());
    }
}
