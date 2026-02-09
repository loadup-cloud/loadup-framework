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

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.github.loadup.commons.dataobject.BaseDO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 服务渠道映射配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("gotone_service_channel")
public class ServiceChannelDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /** 服务代码 */
    private String serviceCode;

    /** 渠道：EMAIL/SMS/PUSH */
    private String channel;

    /** 模板代码 */
    private String templateCode;

    /** 模板内容（支持${var}占位符） */
    private String templateContent;

    /**
     * 渠道配置（JSON格式）
     * EMAIL: {"subject": "xxx", "from": "xxx"}
     * SMS: {"signName": "xxx", "templateId": "xxx"}
     * PUSH: {"title": "xxx", "sound": "default"}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> channelConfig;

    /** 主提供商 */
    private String provider;

    /**
     * 降级提供商列表（JSON格式）
     * ["provider2", "provider3"]
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> fallbackProviders;

    /** 发送策略：SYNC/ASYNC/SCHEDULED */
    private String sendStrategy;

    /**
     * 重试配置（JSON格式）
     * {"maxRetries": 3, "retryInterval": 60}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> retryConfig;

    /** 是否启用 */
    private Boolean enabled;

    /** 优先级（数字越大越优先） */
    private Integer priority;
}

