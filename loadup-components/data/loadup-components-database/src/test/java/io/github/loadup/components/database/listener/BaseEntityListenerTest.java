package io.github.loadup.components.database.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BaseEntityListenerTest {
    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    @Test
    void fillsIdAuditTenantAndLogicalDeleteFields() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        properties.getLogicalDelete().setEnabled(true);
        properties.getLogicalDelete().setNormalValue(2);
        IdGenerator idGenerator = () -> "generated-id";
        BaseEntityListener listener = new BaseEntityListener(properties, idGenerator, Clock.fixed(NOW, ZoneOffset.UTC));
        TestEntity entity = new TestEntity();

        TenantUtil.runWithTenant("tenant-a", () -> listener.onInsert(entity));

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
