package io.github.loadup.components.gotone.api;

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
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;

/**
 * 通知渠道提供商接口
 *
 * <p>每个渠道（EMAIL/SMS/PUSH）都需要实现此接口
 * <p>支持多个提供商实现同一渠道（例如：阿里云短信、腾讯云短信）
 */
public interface NotificationChannelProvider {

    /**
     * 获取渠道类型
     *
     * @return 渠道类型（EMAIL/SMS/PUSH）
     */
    NotificationChannel getChannel();

    /**
     * 获取提供商名称
     *
     * @return 提供商名称（例如：smtp, aliyun, tencent, fcm）
     */
    String getProviderName();

    /**
     * 发送通知
     *
     * @param request 渠道发送请求
     * @return 渠道发送响应
     */
    ChannelSendResponse send(ChannelSendRequest request);

    /**
     * 检查提供商是否可用（配置正确且可连接）
     *
     * @return true 表示可用
     */
    boolean isAvailable();
}


