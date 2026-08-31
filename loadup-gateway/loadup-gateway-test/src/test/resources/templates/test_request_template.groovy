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
