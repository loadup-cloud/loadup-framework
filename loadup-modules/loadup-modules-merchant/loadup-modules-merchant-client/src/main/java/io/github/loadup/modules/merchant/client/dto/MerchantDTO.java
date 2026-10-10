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
package io.github.loadup.modules.merchant.client.dto;

import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import java.time.LocalDateTime;

public record MerchantDTO(
        String id,
        String merchantCode,
        String name,
        String shortName,
        String type,
        String industry,
        String country,
        String province,
        String city,
        @Masked(MaskType.FULL) String address,
        @Masked(MaskType.FULL) String registrationNo,
        @Masked(MaskType.NAME) String contactName,
        @Masked(MaskType.PHONE) String contactPhone,
        @Masked(MaskType.EMAIL) String contactEmail,
        String status,
        long rowVersion,
        String createdBy,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
