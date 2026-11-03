/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.commons.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

class IntegerJsonContractTest {
    public enum Mode {
        INIT,
        SUCCEEDED
    }

    public record EnumValue(Mode mode) {}

    public record Value(long amountMinor, int version, BigDecimal rate) {}

    private final JsonMapper json = JsonUtil.customize(JsonMapper.builder()).build();

    @Test
    void integralFieldsRejectFractionalNumbersAndDecimalRatesRetainPrecision() {
        assertThatThrownBy(() -> json.readValue("{\"amountMinor\":1.5,\"version\":1,\"rate\":0.1}", Value.class))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> json.readValue("{\"amountMinor\":1,\"version\":1.5,\"rate\":0.1}", Value.class))
                .isInstanceOf(RuntimeException.class);
        var value = json.readValue(
                "{\"amountMinor\":9007199254740993,\"version\":1,\"rate\":0.123456789012345678901}", Value.class);
        assertThat(value.amountMinor()).isEqualTo(9007199254740993L);
        assertThat(value.rate()).isEqualByComparingTo(new BigDecimal("0.123456789012345678901"));
    }

    @Test
    void enumsUseDeclaredNamesAndNeverNumericOrdinals() {
        assertThat(json.readValue("{\"mode\":\"INIT\"}", EnumValue.class).mode())
                .isEqualTo(Mode.INIT);
        assertThatThrownBy(() -> json.readValue("{\"mode\":0}", EnumValue.class))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> json.readValue("{\"mode\":\"UNKNOWN\"}", EnumValue.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void customizationRestoresStrictEnumContractOnAPermissiveBuilder() {
        var strict = JsonUtil.customize(JsonMapper.builder().disable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS))
                .build();
        assertThat(strict.readValue("{\"mode\":\"SUCCEEDED\"}", EnumValue.class).mode())
                .isEqualTo(Mode.SUCCEEDED);
        assertThatThrownBy(() -> strict.readValue("{\"mode\":1}", EnumValue.class))
                .isInstanceOf(RuntimeException.class);
    }
}
