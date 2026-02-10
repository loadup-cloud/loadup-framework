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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 通知发送请求（ServiceCode驱动架构）
 *
 * <p>通过 serviceCode 驱动通知发送，后台自动根据配置路由到对应渠道。
 * <p>业务代码与具体渠道解耦，支持动态启用/禁用渠道。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务代码（必填）
     * <p>用于查询通知服务配置，确定发送渠道和模板
     * <p>示例：ORDER_CREATED, USER_REGISTERED, PASSWORD_RESET
     */
    @NotBlank(message = "服务代码不能为空")
    private String serviceCode;

    /**
     * 收件人列表（必填）
     * <p>格式根据渠道自动识别：
     * <ul>
     *   <li>邮箱格式 → EMAIL</li>
     *   <li>手机号格式 → SMS</li>
     *   <li>其他 → PUSH（设备Token或用户ID）</li>
     * </ul>
     */
    @NotEmpty(message = "收件人列表不能为空")
    private List<String> receivers;

    /**
     * 模板参数（必填）
     * <p>用于模板内容渲染，支持 ${varName} 占位符
     * <p>示例：{"userName": "张三", "orderNo": "12345", "amount": "99.00"}
     */
    @NotNull(message = "模板参数不能为空")
    private Map<String, Object> templateParams;

    /**
     * 请求ID（可选，用于幂等性）
     * <p>同一个 requestId 只会发送一次
     * <p>建议使用业务唯一标识，如：ORDER_{orderId}
     */
    private String requestId;

    /**
     * 指定渠道（可选）
     * <p>如果不指定，则根据 serviceCode 配置的所有渠道发送
     * <p>如果指定，则只发送指定渠道
     * <p>示例：["EMAIL", "SMS"]
     */
    private List<String> channels;

    /**
     * 是否异步发送（可选）
     * <p>如果不指定，则根据渠道配置的 sendStrategy 决定
     * <p>true = 异步发送，false = 同步发送
     */
    private Boolean async;
}


