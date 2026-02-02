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

import io.github.loadup.components.extension.api.IExtensionPoint;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;

/** 通知提供者扩展点 所有通知提供者实现此接口并使用 @Extension 注解 */
public interface INotificationProvider extends IExtensionPoint {

    /**
     * 发送通知
     *
     * @param request 通知请求
     * @return 通知响应
     */
    NotificationResponse send(NotificationRequest request);

    /**
     * 批量发送通知
     *
     * @param request 通知请求（包含多个接收人）
     * @return 通知响应
     */
    default NotificationResponse batchSend(NotificationRequest request) {
        return send(request);
    }

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProviderName();

    /**
     * 检查提供商是否可用
     *
     * @return 是否可用
     */
    default boolean isAvailable() {
        return true;
    }
}
