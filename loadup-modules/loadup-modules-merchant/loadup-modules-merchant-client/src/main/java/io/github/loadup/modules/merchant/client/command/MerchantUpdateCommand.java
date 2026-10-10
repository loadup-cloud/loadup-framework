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
package io.github.loadup.modules.merchant.client.command;

import jakarta.validation.constraints.*;

public record MerchantUpdateCommand(
        @NotBlank @Size(max = 64) String id,
        @Min(1) long expectedRowVersion,
        @NotBlank @Size(max = 64) String merchantCode,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 100) String shortName,
        @NotBlank @Size(max = 32) String type,
        @NotBlank @Size(max = 64) String industry,
        @NotBlank @Size(max = 2) String country,
        @Size(max = 64) String province,
        @Size(max = 64) String city,
        @Size(max = 500) String address,
        @Size(max = 64) String registrationNo,
        @Size(max = 100) String contactName,
        @Size(max = 32) String contactPhone,
        @Size(max = 200) String contactEmail) {}
