/*
 * #%L
 * LoadUp Merchant
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
package io.github.loadup.modules.merchant.domain.model;

import io.github.loadup.commons.json.ToStringAsJson;
import java.util.Objects;

public record MerchantBasicInfo(
        String merchantCode,
        String name,
        String shortName,
        MerchantType type,
        String industry,
        String country,
        String province,
        String city,

        @io.github.loadup.commons.masking.Masked(io.github.loadup.commons.masking.MaskType.FULL)
        String address,

        @io.github.loadup.commons.masking.Masked(io.github.loadup.commons.masking.MaskType.FULL)
        String registrationNo,

        @io.github.loadup.commons.masking.Masked(io.github.loadup.commons.masking.MaskType.NAME)
        String contactName,

        @io.github.loadup.commons.masking.Masked(io.github.loadup.commons.masking.MaskType.PHONE)
        String contactPhone,

        @io.github.loadup.commons.masking.Masked(io.github.loadup.commons.masking.MaskType.EMAIL)
        String contactEmail) {
    public MerchantBasicInfo {
        for (String value : new String[] {merchantCode, name, industry, country})
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Required merchant field missing");
        Objects.requireNonNull(type, "Merchant type");
        if (!merchantCode.matches("[A-Za-z0-9][A-Za-z0-9_.-]{0,63}")
                || !industry.matches("[A-Za-z0-9][A-Za-z0-9_.-]{0,63}")
                || !country.matches("[A-Z]{2}"))
            throw new IllegalArgumentException("Invalid merchant code, industry or country");
        bounded(merchantCode, 64);
        bounded(name, 200);
        bounded(shortName, 100);
        bounded(industry, 64);
        bounded(country, 2);
        bounded(province, 64);
        bounded(city, 64);
        bounded(address, 500);
        bounded(registrationNo, 64);
        bounded(contactName, 100);
        bounded(contactPhone, 32);
        bounded(contactEmail, 200);

        if (contactPhone != null && !contactPhone.isEmpty() && !contactPhone.matches("\\+?[0-9]{8,15}"))
            throw new IllegalArgumentException("Invalid contact phone");
        if (contactEmail != null && !contactEmail.isEmpty() && !contactEmail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@.]+"))
            throw new IllegalArgumentException("Invalid contact email");
    }

    private static void bounded(String value, int max) {
        if (value != null && (value.length() > max || value.codePoints().anyMatch(Character::isISOControl)))
            throw new IllegalArgumentException("Invalid merchant field length or control character");
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
