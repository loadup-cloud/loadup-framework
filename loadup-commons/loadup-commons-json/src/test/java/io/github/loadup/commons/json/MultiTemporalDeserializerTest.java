/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

class MultiTemporalDeserializerTest {
    private final JsonMapper mapper = JsonUtil.customize(JsonMapper.builder())
            .defaultTimeZone(TimeZone.getTimeZone("UTC"))
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-29", "2024/02/29", "2024.02.29", "20240229", " 2024-02-29 "})
    void readsStrictLocalDates(String text) {
        assertThat(mapper.readValue(quote(text), LocalDate.class)).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(mapper.readValue(quote(text), Date.class).toInstant())
                .isEqualTo(Instant.parse("2024-02-29T00:00:00Z"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2024-02-29 12:34:56",
                "2024-02-29T12:34:56",
                "2024/02/29 12:34:56",
                "2024.02.29T12:34:56",
                "20240229123456"
            })
    void readsLocalDateTimes(String text) {
        assertThat(mapper.readValue(quote(text), LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2024, 2, 29, 12, 34, 56));
        assertThat(mapper.readValue(quote(text), Date.class).toInstant())
                .isEqualTo(Instant.parse("2024-02-29T12:34:56Z"));
    }

    @Test
    void readsMinutePrecisionAndFractionalSeconds() {
        assertThat(mapper.readValue(quote("2024-02-29T12:34"), LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2024, 2, 29, 12, 34));
        assertThat(mapper.readValue(quote("2024/02/29 12:34:56.123456789"), LocalDateTime.class)
                        .getNano())
                .isEqualTo(123456789);
        assertThat(mapper.readValue(quote("20240229123456.1"), LocalDateTime.class)
                        .getNano())
                .isEqualTo(100000000);
        assertThat(mapper.readValue(quote("2024-02-29 12:34:56.123456789"), Date.class)
                        .toInstant())
                .isEqualTo(Instant.parse("2024-02-29T12:34:56.123Z"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2024-02-29T12:34:56Z", "2024-02-29T20:34:56+08:00",
                "2024-02-29T20:34:56+08:00[Asia/Shanghai]", "Thu, 29 Feb 2024 12:34:56 GMT"
            })
    void datePreservesExplicitInstant(String text) {
        assertThat(mapper.readValue(quote(text), Date.class).toInstant())
                .isEqualTo(Instant.parse("2024-02-29T12:34:56Z"));
    }

    @Test
    void dateUsesMapperZoneForLocalText() {
        JsonMapper shanghai = JsonUtil.customize(JsonMapper.builder())
                .defaultTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                .build();
        assertThat(shanghai.readValue(quote("2024-02-29 20:34:56"), Date.class).toInstant())
                .isEqualTo(Instant.parse("2024-02-29T12:34:56Z"));
        assertThat(shanghai.readValue(quote("2024-02-29"), Date.class).toInstant())
                .isEqualTo(Instant.parse("2024-02-28T16:00:00Z"));
    }

    @Test
    void timestampsAreExplicitMillisecondsForDateOnly() {
        assertThat(mapper.readValue("1709210096000", Date.class).getTime()).isEqualTo(1709210096000L);
        assertThat(mapper.readValue("-1", Date.class).getTime()).isEqualTo(-1L);
        for (Class<?> type : new Class<?>[] {LocalDate.class, LocalDateTime.class}) {
            assertThatThrownBy(() -> mapper.readValue("1709210096000", type)).isInstanceOf(JacksonException.class);
        }
        assertThatThrownBy(() -> mapper.readValue("1709210096.5", Date.class)).isInstanceOf(JacksonException.class);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2023-02-29",
                "2024-02-30",
                "2024-13-01",
                "2024-02-29junk",
                "",
                "01/02/2024",
                "Fri, 30 Feb 2024 12:34:56 GMT"
            })
    void rejectsInvalidDatesAndTrailingContent(String text) {
        for (Class<?> type : new Class<?>[] {Date.class, LocalDate.class, LocalDateTime.class}) {
            assertThatThrownBy(() -> mapper.readValue(quote(text), type)).isInstanceOf(JacksonException.class);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-29T24:00:00", "2024-02-29 12:60:00", "2024-02-29 12:34:56junk"})
    void rejectsInvalidTimes(String text) {
        assertThatThrownBy(() -> mapper.readValue(quote(text), Date.class)).isInstanceOf(JacksonException.class);
        assertThatThrownBy(() -> mapper.readValue(quote(text), LocalDateTime.class))
                .isInstanceOf(JacksonException.class);
    }

    @Test
    void localTypesRejectLossyConversionsAndNullRemainsNull() {
        assertThatThrownBy(() -> mapper.readValue(quote("2024-02-29T12:34:56Z"), LocalDateTime.class))
                .isInstanceOf(JacksonException.class);
        assertThatThrownBy(() -> mapper.readValue(quote("2024-02-29T12:34:56"), LocalDate.class))
                .isInstanceOf(JacksonException.class);
        assertThatThrownBy(() -> mapper.readValue(quote("2024-02-29"), LocalDateTime.class))
                .isInstanceOf(JacksonException.class);
        for (Class<?> type : new Class<?>[] {Date.class, LocalDate.class, LocalDateTime.class}) {
            assertThat(mapper.readValue("null", type)).isNull();
            assertThatThrownBy(() -> mapper.readValue("{}", type)).isInstanceOf(JacksonException.class);
        }
    }

    @Test
    void explicitFieldPatternsAndOutputFormatsRemainAvailable() {
        CustomFormat result =
                mapper.readValue("{\"date\":\"29/02/2024\",\"time\":\"29/02/2024 12:34\"}", CustomFormat.class);
        assertThat(result.date()).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(result.time()).isEqualTo(LocalDateTime.of(2024, 2, 29, 12, 34));
        assertThat(mapper.writeValueAsString(result.date())).isEqualTo(quote("2024-02-29"));
        assertThat(mapper.writeValueAsString(result.time())).isEqualTo(quote("2024-02-29 12:34:00"));
    }

    @Test
    void defaultRecordFieldsUseMultiFormatReaders() {
        DefaultFormat result =
                mapper.readValue("{\"date\":\"2024/02/29\",\"time\":\"20240229123456\"}", DefaultFormat.class);
        assertThat(result.date()).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(result.time()).isEqualTo(LocalDateTime.of(2024, 2, 29, 12, 34, 56));
        assertThat(mapper.writeValueAsString(result))
                .isEqualTo("{\"date\":\"2024-02-29\",\"time\":\"2024-02-29 12:34:56\"}");
    }

    private String quote(String text) {
        return mapper.writeValueAsString(text);
    }

    record CustomFormat(
            @JsonFormat(pattern = "dd/MM/uuuu") LocalDate date,
            @JsonFormat(pattern = "dd/MM/uuuu HH:mm") LocalDateTime time) {}

    record DefaultFormat(LocalDate date, LocalDateTime time) {}
}
