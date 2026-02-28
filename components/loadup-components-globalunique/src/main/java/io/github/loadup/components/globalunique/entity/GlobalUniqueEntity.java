package io.github.loadup.components.globalunique.entity;

/*-
 * #%L
 * LoadUp Components :: Global Unique
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局唯一性记录实体
 *
 * @author loadup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalUniqueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 唯一键（业务方自定义）
     */
    private String uniqueKey;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID（可选）
     */
    private String bizId;

    /**
     * 请求数据快照（可选，JSON格式）
     */
    private String requestData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
