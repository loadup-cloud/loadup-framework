/*-
 * #%L
 * LoadUp Gateway Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
// 测试响应模板 - 处理响应并添加统一格式
import io.github.loadup.gateway.facade.model.GatewayResponse
import io.github.loadup.commons.util.JsonUtil

// 添加通用响应头
response.headers.put("X-Gateway-Response-Processed", "true")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 处理响应体，统一格式
if (response.body != null && response.statusCode == 200) {
    def responseData

    try {
        responseData = JsonUtil.toMap(response.body)
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

    response.body = JsonUtil.toJson(unifiedResponse)
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

    response.body = JsonUtil.toJson(errorResponse)
    response.contentType = "application/json"
}

log.info("Response template processed for: {}", response.requestId)

return response
