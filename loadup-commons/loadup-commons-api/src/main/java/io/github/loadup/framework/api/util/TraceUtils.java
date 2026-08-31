package io.github.loadup.framework.api.util;

/*-
 * #%L
 * Loadup Common Api
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.util.UUID;

/**
 * 链路追踪工具类 利用 ThreadLocal 存储当前线程的 TraceID
 */
public final class TraceUtils {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceUtils() {}

    /**
     * 初始化 TraceId 如果传入的为空，则自动生成一个
     */
    public static String initTraceId(String traceId) {
        if (null != traceId && !traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TRACE_ID_HOLDER.set(traceId);
        return traceId;
    }

    /**
     * 获取当前线程的 TraceId
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏（非常重要！） 建议在 Web 拦截器的 afterCompletion 中调用
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
