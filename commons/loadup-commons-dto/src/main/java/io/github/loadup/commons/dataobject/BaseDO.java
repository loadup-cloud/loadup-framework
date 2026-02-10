package io.github.loadup.commons.dataobject;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.mybatisflex.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Base Data Object for MyBatis-Flex
 *
 * <p>所有实体类的基类，提供通用字段。
 *
 * <p>子类需要添加 MyBatis-Flex 注解：@Table, @Id, @Column 等
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Getter
@Setter
public abstract class BaseDO implements Serializable {
    // @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    @Id
    private String id;

    /** 创建时间（自动填充） */
    // @Column(onInsertValue = "now()")
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    // @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updatedAt;

    /**
     * Tenant ID (optional, controlled by loadup.database.multi-tenant.enabled)
     *
     * <p>When multi-tenant is enabled in database configuration, this field will be used for tenant
     * isolation. Queries will automatically filter by tenant_id, and inserts/updates will
     * automatically set tenant_id from TenantContextHolder.
     */
    // @Column(tenantId = true)
    private String tenantId;

    /**
     * Logical delete flag (optional, controlled by loadup.database.logical-delete.enabled)
     *
     * <p>When logical delete is enabled in database configuration, this field will be used to mark
     * deleted records. Default value is false (not deleted).
     */
    // @Column(isLogicDelete = true)
    private Boolean deleted = false;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
