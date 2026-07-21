/*-
 * #%L
 * LoadUp Gateway Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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
// 高级请求模板 - 支持数据转换和路由决策
import io.github.loadup.gateway.facade.model.GatewayRequest
import io.github.loadup.gateway.facade.utils.JsonUtils

// 用户认证和权限检查
def token = request.headers.get("Authorization")
if (token != null && token.startsWith("Bearer ")) {
    request.attributes.put("authenticated", true)
    request.attributes.put("token", token.substring(7))
} else {
    request.attributes.put("authenticated", false)
}

// API版本处理
def apiVersion = request.headers.get("API-Version") ?: "v1"
request.attributes.put("apiVersion", apiVersion)

// 请求体数据转换
if (request.body != null && !request.body.trim().isEmpty()) {
    try {
        def bodyMap = JsonUtils.toMap(request.body)

        // 数据清洗和标准化
        if (bodyMap.containsKey("phone")) {
            // 标准化手机号格式
            def phone = bodyMap.phone.toString().replaceAll("[^0-9]", "")
            bodyMap.put("phone", phone)
        }

        if (bodyMap.containsKey("email")) {
            // 邮箱转小写
            bodyMap.put("email", bodyMap.email.toString().toLowerCase())
        }

        // 添加请求元数据
        bodyMap.put("_meta", [
            "requestId": request.requestId,
            "timestamp": System.currentTimeMillis(),
            "source": "gateway",
            "version": apiVersion
        ])

        request.body = JsonUtils.toJson(bodyMap)

    } catch (Exception e) {
        log.warn("Failed to process request body: {}", e.message)
        // 添加错误标记但不阻止请求
        request.attributes.put("bodyProcessError", e.message)
    }
}

// 请求速率限制标记
def clientIp = request.clientIp
request.attributes.put("rateLimitKey", "ip:" + clientIp)

// 日志记录
log.info("Advanced request template processed - Method: {}, Path: {}, Auth: {}",
         request.method, request.path, request.attributes.get("authenticated"))

return request
