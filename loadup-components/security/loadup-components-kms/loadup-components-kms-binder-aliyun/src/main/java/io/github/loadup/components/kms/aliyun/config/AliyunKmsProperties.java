/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.kms.aliyun.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.kms.aliyun", ignoreUnknownFields = false)
public record AliyunKmsProperties(
        String regionId, String endpoint, Duration connectTimeout, Duration readTimeout, Credentials credentials) {
    public AliyunKmsProperties {
        connectTimeout = timeout(connectTimeout, Duration.ofSeconds(3));
        readTimeout = timeout(readTimeout, Duration.ofSeconds(10));
    }

    private static Duration timeout(Duration value, Duration fallback) {
        if (value == null) {
            return fallback;
        }
        if (value.compareTo(Duration.ofMillis(1)) < 0 || value.compareTo(Duration.ofMinutes(2)) > 0) {
            throw new IllegalArgumentException("Aliyun KMS timeout must be between 1 ms and 2 minutes");
        }
        return value;
    }

    public enum CredentialType {
        ECS_RAM_ROLE,
        ACCESS_KEY
    }

    public record Credentials(CredentialType type, String roleName) {}
}
