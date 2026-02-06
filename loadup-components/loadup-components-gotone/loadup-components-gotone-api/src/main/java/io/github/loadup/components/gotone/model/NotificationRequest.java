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

import io.github.loadup.components.gotone.enums.NotificationChannel;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** 通知请求 */
@Data
@Builder
public class NotificationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 业务ID（用于幂等） */
    private String bizId;

    /** 渠道 */
    private NotificationChannel channel;

    /** 接收人列表 */
    private List<String> addressList;

    /** 模板代码 */
    private String templateCode;

    /** 模板参数 */
    private Map<String, Object> templateParams;

    /** 标题（用于邮件、站内信等） */
    private String title;

    /** 内容（当不使用模板时） */
    private String content;

    /** 附加数据 */
    private Map<String, Object> extraData;

    /** 业务场景（用于扩展点） */
    private String bizScenario;

    /** 优先级（1-10，10最高） */
    @Builder.Default
    private Integer priority = 5;

    /** 是否异步发送 */
    @Builder.Default
    private Boolean async = true;

    /** 提供商列表（用于多提供商降级） */
    private List<String> providers;
}
