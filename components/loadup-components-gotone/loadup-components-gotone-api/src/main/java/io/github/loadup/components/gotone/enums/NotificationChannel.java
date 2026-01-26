package io.github.loadup.components.gotone.enums;

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

/**
 * 通知渠道枚举
 */
public enum NotificationChannel {
    /**
     * 邮件
     */
    EMAIL,

    /**
     * 短信
     */
    SMS,

    /**
     * 推送消息
     */
    PUSH,

    /**
     * 站内信
     */
    INTERNAL_MESSAGE,

    /**
     * 微信
     */
    WECHAT,

    /**
     * 钉钉
     */
    DINGTALK,

    /**
     * 飞书
     */
    FEISHU
}

