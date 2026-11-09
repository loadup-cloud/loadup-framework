package io.github.loadup.components.payment.channel.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.loadup.commons.error.DomainViolation;
import io.github.loadup.commons.money.Money;
import org.junit.jupiter.api.Test;

class ChannelAmountsTest {
    @Test
    void acceptsZeroAndTheLargestCnyMinorAmountWithoutRounding() {
        assertEquals(Money.ofMinor(0, "CNY"), ChannelAmounts.cnyAmount(0, "CNY"));
        assertEquals(Long.MAX_VALUE, ChannelAmounts.cnyAmount(Long.MAX_VALUE, "CNY").getCent());
        ChannelAmounts.cnyAmount(Money.ofMinor(0, "CNY"));
    }

    @Test
    void preservesCurrencyAndNegativeAmountFailureReasonsForBothOverloads() {
        assertEquals(
                DomainViolation.Reason.CURRENCY_MISMATCH,
                assertThrows(DomainViolation.class, () -> ChannelAmounts.cnyAmount(1, "USD")).reason());
        assertEquals(
                DomainViolation.Reason.CURRENCY_MISMATCH,
                assertThrows(DomainViolation.class, () -> ChannelAmounts.cnyAmount(Money.ofMinor(1, "USD")))
                        .reason());
        assertEquals(
                DomainViolation.Reason.INVALID_ARGUMENT,
                assertThrows(DomainViolation.class, () -> ChannelAmounts.cnyAmount(-1, "CNY")).reason());
        assertEquals(
                DomainViolation.Reason.INVALID_ARGUMENT,
                assertThrows(DomainViolation.class, () -> ChannelAmounts.cnyAmount(Money.ofMinor(-1, "CNY")))
                        .reason());
    }

    @Test
    void rejectsMissingMoneyAndCurrencyWithoutRestrictingTheGenericMoneyType() {
        assertThrows(NullPointerException.class, () -> ChannelAmounts.cnyAmount(null));
        assertEquals(
                DomainViolation.Reason.CURRENCY_MISMATCH,
                assertThrows(DomainViolation.class, () -> ChannelAmounts.cnyAmount(1, null)).reason());
        assertEquals(-1, Money.ofMinor(-1, "USD").getCent());
    }
}
