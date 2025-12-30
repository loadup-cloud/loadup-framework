package com.github.loadup.components.gotone.dataobject;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 业务代码数据对象
 */
@Getter
@Setter
@Table("gotone_business_code")
public class BusinessCodeDO extends BaseDO implements Persistable<String> {

    @Id
    @Column("id")
    private String id;

    @Column("business_code")
    private String businessCode;

    @Column("business_name")
    private String businessName;

    @Column("description")
    private String description;

    @Column("enabled")
    private Boolean enabled;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        // 如果 createdAt 为 null，说明是新实体（还未持久化）
        return getCreatedAt() == null;
    }

    // 用于测试时标记为非新实体（可选）
    public void markNotNew() {
        this.isNew = false;
    }
}

