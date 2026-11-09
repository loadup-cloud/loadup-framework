package io.github.loadup.components.payment.channel.contract;

import io.github.loadup.commons.error.DomainViolation;
import io.github.loadup.commons.money.Money;
import java.util.Objects;

/**
 * Amount policy shared by acquiring channel contracts and their consumers.
 */
public final class ChannelAmounts {
    private ChannelAmounts() {}

    /**
     * Acquiring accepts non-negative CNY amounts; the framework Money type also supports other cases.
     */
    public static void cnyAmount(Money value) {
        Objects.requireNonNull(value);
        DomainViolation.require("CNY".equals(value.getCurrencyValue()), DomainViolation.Reason.CURRENCY_MISMATCH);
        DomainViolation.require(value.getCent() >= 0, DomainViolation.Reason.INVALID_ARGUMENT);
    }

    /**
     * Validate the acquiring boundary before creating the shared immutable Money value.
     */
    public static Money cnyAmount(long minor, String currency) {
        DomainViolation.require("CNY".equals(currency), DomainViolation.Reason.CURRENCY_MISMATCH);
        DomainViolation.require(minor >= 0, DomainViolation.Reason.INVALID_ARGUMENT);
        return Money.ofMinor(minor, currency);
    }
}
