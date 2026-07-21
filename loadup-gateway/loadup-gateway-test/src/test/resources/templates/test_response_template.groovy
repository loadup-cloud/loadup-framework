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
// 测试响应模板 - 处理响应并添加统一格式
import io.github.loadup.gateway.facade.model.GatewayResponse
import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用响应头
response.headers.put("X-Gateway-Response-Processed", "true")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 处理响应体，统一格式
if (response.body != null && response.statusCode == 200) {
    def responseData

    try {
        responseData = JsonUtils.toMap(response.body)
    } catch (Exception e) {
        // 如果不是JSON，包装成JSON格式
        responseData = ["data": response.body]
    }

    // 构建统一响应格式
    def unifiedResponse = [
        "code": 200,
        "message": "success",
        "data": responseData,
        "meta": [
            "requestId": response.requestId,
            "timestamp": System.currentTimeMillis(),
            "processingTime": response.processingTime
        ]
    ]

    response.body = JsonUtils.toJson(unifiedResponse)
    response.contentType = "application/json"

} else if (response.statusCode >= 400) {
    // 处理错误响应
    def errorResponse = [
        "code": response.statusCode,
        "message": response.errorMessage ?: "Unknown error",
        "data": null,
        "meta": [
            "requestId": response.requestId,
            "timestamp": System.currentTimeMillis(),
            "processingTime": response.processingTime
        ]
    ]

    response.body = JsonUtils.toJson(errorResponse)
    response.contentType = "application/json"
}

log.info("Response template processed for: {}", response.requestId)

return response
