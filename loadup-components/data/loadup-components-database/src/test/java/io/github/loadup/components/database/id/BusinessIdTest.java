/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.sequence.DatabaseResultCodeEnum;
import io.github.loadup.components.database.sequence.Sequence;
import io.github.loadup.components.database.sequence.SequenceScope;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class BusinessIdTest {
    private final BusinessIdFormatter formatter = new BusinessIdFormatter();

    private BusinessIdCommand command(String extension) {
        return new BusinessIdCommand(0, 1, "11000000", extension, 1, 440);
    }

    @Test
    void preservesFixedVectorAndLeadingZeros() {
        BusinessIdParts parts = new BusinessIdParts(LocalDate.of(2026, 10, 12), command(""), 12345678);
        String id = formatter.format(parts);
        assertThat(id).isEqualTo("20261012011100000000144012345678");
        assertThat(id).hasSize(32);
        assertThat(formatter.parse(id)).isEqualTo(parts);
    }

    @Test
    void supportsMaximumLength() {
        BusinessIdParts parts = new BusinessIdParts(LocalDate.of(2026, 10, 12), command("A".repeat(32)), 99_999_999);
        String id = formatter.format(parts);
        assertThat(id).hasSize(64);
        assertThat(formatter.parse(id)).isEqualTo(parts);
    }

    @Test
    void rejectsInvalidFieldsAndSequence() {
        assertThatThrownBy(() -> command("A".repeat(33))).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> formatter.format(new BusinessIdParts(LocalDate.now(), command(""), 100_000_000)))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> formatter.format(new BusinessIdParts(LocalDate.now(), command(""), 0)))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> new BusinessIdCommand(1, 1, "abcdefgh", "", 1000, 0))
                .isInstanceOf(CommonException.class);
    }

    @Test
    void rejectsInvalidDateAndZeroSequence() {
        assertThatThrownBy(() -> formatter.parse("20260230011100000000144012345678"))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> formatter.parse("20261012011100000000144000000000"))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> formatter.parse("")).isInstanceOf(CommonException.class);
    }

    @Test
    void capturesLocalDateAndBatchScope() {
        AtomicReference<SequenceScope> seen = new AtomicReference<>();
        Sequence sequence = new Sequence() {
            public long next(SequenceScope s) {
                return 1;
            }

            public long[] nextBatch(SequenceScope s, int n) {
                seen.set(s);
                return new long[] {1, 2};
            }
        };
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T16:30:00Z"), ZoneId.of("Asia/Shanghai"));
        String[] ids = new BusinessIdGenerator(sequence, clock, formatter).generateBatch(command(""), 2);
        assertThat(ids).allMatch(id -> id.startsWith("20261012"));
        assertThat(seen.get().date()).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(ids[0]).endsWith("00000001");
        assertThat(ids[1]).endsWith("00000002");
    }

    @Test
    void singleGenerationUsesSingleNumericClaim() {
        Sequence sequence = new Sequence() {
            public long next(SequenceScope scope) {
                return 7;
            }

            public long[] nextBatch(SequenceScope scope, int count) {
                throw new AssertionError("Single generation must not allocate batch arrays");
            }
        };
        BusinessIdGenerator generator = new BusinessIdGenerator(
                sequence, Clock.fixed(Instant.parse("2026-10-12T00:00:00Z"), ZoneOffset.UTC), formatter);
        assertThat(generator.generate(command(""))).hasSize(32).endsWith("00000007");
    }

    @Test
    void rejectsEveryOutOfBoundsMetadataFieldWithStableCode() {
        BusinessIdCommand[] valid = {
            new BusinessIdCommand(0, 0, "00000000", null, 0, 0),
            new BusinessIdCommand(9, 9, "99999999", "aB09", 999, 999)
        };
        for (BusinessIdCommand c : valid) {
            var parts = new BusinessIdParts(LocalDate.of(2024, 2, 29), c, 1);
            assertThat(formatter.parse(formatter.format(parts))).isEqualTo(parts);
        }
        assertThat(valid[0].extensionCode()).isEmpty();
        java.util.List<java.util.function.Supplier<BusinessIdCommand>> invalid = java.util.List.of(
                () -> new BusinessIdCommand(-1, 1, "11000000", "", 1, 1),
                () -> new BusinessIdCommand(10, 1, "11000000", "", 1, 1),
                () -> new BusinessIdCommand(1, -1, "11000000", "", 1, 1),
                () -> new BusinessIdCommand(1, 10, "11000000", "", 1, 1),
                () -> new BusinessIdCommand(1, 1, null, "", 1, 1),
                () -> new BusinessIdCommand(1, 1, "1234567", "", 1, 1),
                () -> new BusinessIdCommand(1, 1, "123456789", "", 1, 1),
                () -> new BusinessIdCommand(1, 1, "abcdefgh", "", 1, 1),
                () -> new BusinessIdCommand(1, 1, "11000000", "bad-value", 1, 1),
                () -> new BusinessIdCommand(1, 1, "11000000", "", -1, 1),
                () -> new BusinessIdCommand(1, 1, "11000000", "", 1000, 1),
                () -> new BusinessIdCommand(1, 1, "11000000", "", 1, -1),
                () -> new BusinessIdCommand(1, 1, "11000000", "", 1, 1000));
        for (var factory : invalid) {
            assertThatThrownBy(factory::get)
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.INVALID_INPUT));
        }
    }

    @Test
    void rejectsIncompletePartsAndYearsOutsideFourDigitPositiveRange() {
        for (BusinessIdParts parts : new BusinessIdParts[] {
            null,
            new BusinessIdParts(null, command(""), 1),
            new BusinessIdParts(LocalDate.now(), null, 1),
            new BusinessIdParts(LocalDate.of(0, 1, 1), command(""), 1),
            new BusinessIdParts(LocalDate.of(10000, 1, 1), command(""), 1)
        }) {
            assertThatThrownBy(() -> formatter.format(parts))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.INVALID_INPUT));
        }
        for (LocalDate date : java.util.List.of(LocalDate.of(1, 1, 1), LocalDate.of(9999, 12, 31))) {
            var parts = new BusinessIdParts(date, command(""), BusinessIdFormatter.MAX_SEQUENCE);
            assertThat(formatter.parse(formatter.format(parts))).isEqualTo(parts);
        }
    }

    @Test
    void rejectsMalformedEncodedPrefixExtensionAndTail() {
        String valid = formatter.format(new BusinessIdParts(LocalDate.of(2026, 10, 12), command(""), 1));
        for (String malformed : new String[] {
            null,
            "0".repeat(31),
            "0".repeat(65),
            "x" + valid.substring(1),
            valid.substring(0, 18) + "_" + valid.substring(18),
            valid.substring(0, valid.length() - 1) + "x",
            "00000000" + valid.substring(8),
            "20230229" + valid.substring(8)
        }) {
            assertThatThrownBy(() -> formatter.parse(malformed))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.INVALID_INPUT));
        }
    }

    @Test
    void invalidGeneratorMetadataNeverReservesSequenceNumbers() {
        Sequence sequence = new Sequence() {
            public long next(SequenceScope scope) {
                throw new AssertionError("Invalid metadata must not allocate");
            }

            public long[] nextBatch(SequenceScope scope, int count) {
                throw new AssertionError("Invalid metadata must not allocate");
            }
        };
        Clock ordinary = Clock.fixed(Instant.parse("2026-10-12T00:00:00Z"), ZoneOffset.UTC);
        assertThatThrownBy(() -> new BusinessIdGenerator(sequence, ordinary, formatter, null))
                .isInstanceOf(CommonException.class);
        BusinessIdGenerator generator = new BusinessIdGenerator(sequence, ordinary, formatter);
        assertThatThrownBy(() -> generator.generate(null)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> generator.generateBatch(null, 2)).isInstanceOf(CommonException.class);
        for (String instant : new String[] {"0000-01-01T00:00:00Z", "+10000-01-01T00:00:00Z"}) {
            var invalidDateGenerator =
                    new BusinessIdGenerator(sequence, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), formatter);
            assertThatThrownBy(() -> invalidDateGenerator.generate(command(""))).isInstanceOf(CommonException.class);
        }
    }
}
