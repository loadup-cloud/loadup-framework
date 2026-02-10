package io.github.loadup.components.gotone.core.dataobject;

/*-
 * #%L
 * loadup-components-gotone-core
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

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 通知服务配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("gotone_notification_service")
public class NotificationServiceDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /** 服务代码（唯一） */
    private String serviceCode;

    /** 服务名称 */
    private String serviceName;

    /** 服务描述 */
    private String description;

    /** 是否启用 */
    private Boolean enabled;

    /** 优先级 */
    private Integer priority;
}
