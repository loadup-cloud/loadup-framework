package io.github.loadup.components.gotone.model;

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

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 通知发送响应
 */
@Data
@Builder
public class NotificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 追踪ID（批量发送使用同一个traceId） */
    private String traceId;

    /** 服务代码 */
    private String serviceCode;

    /** 总收件人数 */
    private Integer totalReceivers;

    /** 各渠道发送结果 */
    private List<ChannelSendResult> channelResults;

    /** 是否成功（至少一个渠道发送成功） */
    private Boolean success;

    /** 错误信息（如果全部失败） */
    private String errorMessage;

    /**
     * 渠道发送结果
     */
    @Data
    @Builder
    public static class ChannelSendResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 渠道：EMAIL/SMS/PUSH */
        private String channel;

        /** 使用的提供商 */
        private String provider;

        /** 总收件人数 */
        private Integer totalReceivers;

        /** 成功数 */
        private Integer successCount;

        /** 失败数 */
        private Integer failedCount;

        /** 是否成功 */
        private Boolean success;

        /** 错误信息 */
        private String errorMessage;
    }
}
