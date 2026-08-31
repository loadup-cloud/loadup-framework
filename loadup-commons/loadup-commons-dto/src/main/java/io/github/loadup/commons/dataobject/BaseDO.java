package io.github.loadup.commons.dataobject;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.mybatisflex.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
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
public abstract class BaseDO implements Serializable {
    // @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    @Id
    private String id;

    /**
     * 创建时间（自动填充）
     */
    // @Column(onInsertValue = "now()")
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动填充）
     */
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

    public String getId() {
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public Boolean isDeleted() {
        return this.deleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
