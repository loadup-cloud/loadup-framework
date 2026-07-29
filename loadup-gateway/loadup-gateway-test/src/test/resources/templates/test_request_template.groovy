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
// 测试请求模板 - 处理入参并添加通用字段
import io.github.loadup.gateway.facade.model.GatewayRequest
import io.github.loadup.commons.util.JsonUtil

// 添加通用请求头
request.headers.put("X-Gateway-Processed", "true")
request.headers.put("X-Request-Time", request.requestTime.toString())
request.headers.put("X-Request-Id", request.requestId)

// 如果是POST请求，处理请求体
if (request.method == "POST" && request.body != null) {
    def bodyMap = JsonUtil.toMap(request.body)

    // 添加系统字段
    bodyMap.put("_system", [
        "requestId": request.requestId,
        "timestamp": System.currentTimeMillis(),
        "clientIp": request.clientIp,
        "userAgent": request.userAgent
    ])

    // 数据验证
    if (bodyMap.containsKey("name") && bodyMap.name.length() > 50) {
        bodyMap.put("_validation", ["nameLength": "too_long"])
    }

    request.body = JsonUtil.toJson(bodyMap)
}

// 添加查询参数处理
if (request.queryParameters.containsKey("transform")) {
    request.attributes.put("needTransform", true)
}

log.info("Request template processed for: {}", request.requestId)

return request
