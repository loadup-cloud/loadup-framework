/*
 * #%L
 * LoadUp Common Masking
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
package io.github.loadup.commons.masking;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MaskingTest {
    @Test
    void masksCommonValues() {
        assertThat(Masking.mask("13812345678", MaskType.PHONE)).isEqualTo("138****5678");
        assertThat(Masking.mask("alice@example.com", MaskType.EMAIL)).isEqualTo("a****@example.com");
        assertThat(Masking.mask("a@example.com", MaskType.EMAIL)).isEqualTo("*@example.com");
        assertThat(Masking.mask("11010119900101123X", MaskType.ID_CARD)).isEqualTo("110***********123X");
        assertThat(Masking.mask("6222021234567890", MaskType.BANK_CARD)).isEqualTo("************7890");
        assertThat(Masking.mask("Alice", MaskType.NAME)).isEqualTo("A****");
        assertThat(Masking.mask("secret", MaskType.FULL)).isEqualTo("******");
    }

    @Test
    void neverExposesShortOrMalformedValues() {
        for (MaskType type : new MaskType[] {MaskType.PHONE, MaskType.EMAIL, MaskType.ID_CARD, MaskType.BANK_CARD}) {
            assertThat(Masking.mask("abc", type)).isEqualTo("******");
        }
        assertThat(Masking.mask("A", MaskType.NAME)).isEqualTo("******");
        assertThat(Masking.keep("1234", 3, 4)).isEqualTo("******");
        assertThat(Masking.keep("123\n456", 1, 1)).isEqualTo("******");
        assertThat(Masking.mask("  ", MaskType.NAME)).isEqualTo("******");
    }

    @Test
    void preservesAbsenceAndUsesCodePoints() {
        assertThat(Masking.mask(null, MaskType.PHONE)).isNull();
        assertThat(Masking.mask("", MaskType.PHONE)).isEmpty();
        String supplementary = new String(Character.toChars(0x20000));
        assertThat(Masking.keep(supplementary + "ab", 1, 1)).isEqualTo(supplementary + "*b");
        assertThat(Masking.mask(supplementary, MaskType.NAME)).isEqualTo("******");
    }

    @Test
    void rejectsInvalidRules() {
        assertThatIllegalArgumentException().isThrownBy(() -> Masking.keep("secret", -1, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> Masking.keep("secret", 0, 33));
        assertThatIllegalArgumentException().isThrownBy(() -> Masking.mask("secret", MaskType.CUSTOM));
    }
}
