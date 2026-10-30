/*
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.modules.upms.infrastructure.converter.UserOAuthBindingConverter;
import io.github.loadup.modules.upms.infrastructure.converter.UserOAuthBindingConverterImpl;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class OAuthBindingMappingTest {
    @Test
    void springMapperPreservesBindingAndAuditFieldsWithoutCopyingTenantAuthority() {
        try (var context = new AnnotationConfigApplicationContext(UserOAuthBindingConverterImpl.class)) {
            var converter = context.getBean(UserOAuthBindingConverter.class);
            var stored = new UserOAuthBindingDO();
            stored.setId("binding");
            stored.setTenantId("trusted-tenant");
            stored.setDeleted(false);
            stored.setUserId("user");
            stored.setProvider("test");
            stored.setOpenId("subject");
            stored.setAccessToken("access-secret");
            stored.setRefreshToken("refresh-secret");
            stored.setCreatedAt(LocalDateTime.of(2026, 10, 11, 0, 0));
            stored.setUpdatedAt(stored.getCreatedAt());
            stored.setBoundAt(stored.getCreatedAt());
            stored.setExpiresAt(stored.getCreatedAt().plusHours(1));
            var roundtrip = converter.toDO(converter.toEntity(stored));
            assertThat(roundtrip)
                    .usingRecursiveComparison()
                    .ignoringFields("tenantId", "deleted")
                    .isEqualTo(stored);
            assertThat(roundtrip.getTenantId()).isNull();
        }
    }
}
