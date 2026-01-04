package com.github.loadup.commons.dataobject;

/*-
 * #%L
 * loadup-commons-lang
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

import com.github.loadup.commons.util.ToStringUtils;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;

@Getter
@Setter
public abstract class BaseDO implements Serializable {

  @CreatedDate @InsertOnlyProperty private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  /**
   * Tenant ID (optional, controlled by loadup.database.multi-tenant.enabled)
   *
   * <p>When multi-tenant is enabled in database configuration, this field will be used for tenant
   * isolation. Queries will automatically filter by tenant_id, and inserts/updates will
   * automatically set tenant_id from TenantContextHolder.
   */
  private String tenantId;

  /**
   * Logical delete flag (optional, controlled by loadup.database.logical-delete.enabled)
   *
   * <p>When logical delete is enabled in database configuration, this field will be used to mark
   * deleted records. Default value is false (not deleted).
   */
  private Boolean deleted = false;

  public abstract String getId();

  public abstract void setId(String id);

  @Override
  public String toString() {
    return ToStringUtils.reflectionToString(this);
  }
}
