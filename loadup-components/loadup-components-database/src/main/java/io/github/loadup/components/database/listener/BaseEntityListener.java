package io.github.loadup.components.database.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

/** Populates common fields before MyBatis-Flex insert and update operations. */
public class BaseEntityListener implements InsertListener, UpdateListener {
    private final DatabaseProperties databaseProperties;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @Override
    public void onInsert(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        DatabaseProperties.Audit audit = databaseProperties.getAudit();
        DatabaseProperties.IdGenerator idProperties = databaseProperties.getIdGenerator();
        LocalDateTime now = LocalDateTime.now(clock);

        if (idProperties.isEnabled() && !StringUtils.hasText(baseDO.getId())) {
            baseDO.setId(idGenerator.generate());
        }

        if (audit.isEnabled()) {
            if (baseDO.getCreatedAt() == null) {
                baseDO.setCreatedAt(now);
            }
            baseDO.setUpdatedAt(now);
        }

        DatabaseProperties.MultiTenant tenant = databaseProperties.getMultiTenant();
        if (tenant.isEnabled()) {
            String tenantId = TenantUtil.getTenantId();
            if (!StringUtils.hasText(tenantId)) {
                tenantId = tenant.getDefaultTenantId();
            }
            if (!StringUtils.hasText(tenantId) && tenant.isRequired()) {
                throw new TenantContextMissingException(entity.getClass());
            }
            if (StringUtils.hasText(tenantId)) {
                baseDO.setTenantId(tenantId);
            }
        }

        DatabaseProperties.LogicalDelete logicalDelete = databaseProperties.getLogicalDelete();
        if (logicalDelete.isEnabled()) {
            baseDO.setDeleted(logicalDelete.getNormalValue());
        } else if (baseDO.getDeleted() == null) {
            baseDO.setDeleted(0);
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        if (databaseProperties.getAudit().isEnabled()) {
            baseDO.setUpdatedAt(LocalDateTime.now(clock));
        }
    }

    public BaseEntityListener(DatabaseProperties databaseProperties, IdGenerator idGenerator, Clock clock) {
        this.databaseProperties = databaseProperties;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }
}
