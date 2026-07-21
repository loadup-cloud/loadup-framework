/*-
 * #%L
 * Loadup Launcher
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
package templates
// 测试响应模板 - 处理响应并添加统一格式

import io.github.loadup.gateway.facade.utils.JsonUtils

// 添加通用响应头
response.headers.put("X-Gateway-Response-Processed", "true")
response.headers.put("X-Response-Time", response.responseTime.toString())
response.headers.put("X-Processing-Time", response.processingTime.toString())

// 处理响应体，统一格式
if (response.body != null && response.statusCode == 200) {
    def responseData


    responseData = JsonUtils.toMap(response.body)
    if (responseData == null) {
        // 如果不是JSON，包装成JSON格式
        responseData = response.body
    }


    // 构建统一响应格式
    def unifiedResponse = ["data": responseData,
                           "meta": ["requestId"     : response.requestId,
                                    "timestamp"     : System.currentTimeMillis(),
                                    "processingTime": response.processingTime]]

    response.body = JsonUtils.toJson(responseData)
    response.contentType = "application/json"

}

log.info("Response template processed for: {}", response.requestId)

return response
