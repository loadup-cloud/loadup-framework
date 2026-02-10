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
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 通知发送记录 - 单表+JSON扩展字段架构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("gotone_notification_record")
public class NotificationRecordDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /** 服务代码（业务标识） */
    private String serviceCode;

    /** 追踪ID - 批量发送时相同 */
    private String traceId;

    /** 请求ID - 用于幂等性 */
    private String requestId;

    /** 渠道：EMAIL/SMS/PUSH */
    private String channel;

    /** 实际使用的提供商 */
    private String provider;

    /** 收件人（邮箱/手机号/设备Token） */
    private String receiver;

    /** 模板代码 */
    private String templateCode;

    /** 实际发送内容 */
    private String content;

    /**
     * 渠道扩展数据（JSON格式）
     * EMAIL: {"subject": "xxx", "cc": [], "bcc": [], "attachments": []}
     * SMS: {"phoneNumber": "xxx", "signName": "xxx", "templateId": "xxx"}
     * PUSH: {"title": "xxx", "badge": 1, "sound": "default", "extras": {}}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> channelData;

    /** 状态：PENDING/SUCCESS/FAILED/RETRY */
    private String status;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetries;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 成功时间 */
    private LocalDateTime successTime;
}
