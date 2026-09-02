/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

package io.github.loadup.components.database.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.tenant.TenantContextHolder;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BaseEntityListenerTest {
    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    @Test
    void fillsIdAuditTenantAndLogicalDeleteFields() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        properties.getLogicalDelete().setEnabled(true);
        properties.getLogicalDelete().setNormalValue(2);
        TenantContextHolder.setTenantId("tenant-a");
        IdGenerator idGenerator = () -> "generated-id";
        BaseEntityListener listener = new BaseEntityListener(properties, idGenerator, Clock.fixed(NOW, ZoneOffset.UTC));
        TestEntity entity = new TestEntity();

        listener.onInsert(entity);

        assertThat(entity.getId()).isEqualTo("generated-id");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW.atZone(ZoneOffset.UTC).toLocalDateTime());
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(entity.getTenantId()).isEqualTo("tenant-a");
        assertThat(entity.getDeleted()).isEqualTo(2);
    }

    @Test
    void rejectsInsertWithoutRequiredTenant() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        BaseEntityListener listener = new BaseEntityListener(properties, () -> "id", Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatIllegalStateException().isThrownBy(() -> listener.onInsert(new TestEntity()));
    }

    @Test
    void refreshesUpdatedAtWithoutChangingCreatedAt() {
        DatabaseProperties properties = new DatabaseProperties();
        BaseEntityListener listener = new BaseEntityListener(properties, () -> "id", Clock.fixed(NOW, ZoneOffset.UTC));
        TestEntity entity = new TestEntity();
        entity.setCreatedAt(NOW.minusSeconds(60).atZone(ZoneOffset.UTC).toLocalDateTime());

        listener.onUpdate(entity);

        assertThat(entity.getUpdatedAt()).isEqualTo(NOW.atZone(ZoneOffset.UTC).toLocalDateTime());
        assertThat(entity.getCreatedAt()).isBefore(entity.getUpdatedAt());
    }

    private static final class TestEntity extends BaseDO {
        private static final long serialVersionUID = 1L;
    }
}
