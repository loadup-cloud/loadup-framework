package io.github.loadup.framework.api.util;

/*-
 * #%L
 * Loadup Common Api
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.util.UUID;

/** 链路追踪工具类 利用 ThreadLocal 存储当前线程的 TraceID */
public final class TraceUtils {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceUtils() {}

    /** 初始化 TraceId 如果传入的为空，则自动生成一个 */
    public static String initTraceId(String traceId) {
        if (null != traceId && !traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TRACE_ID_HOLDER.set(traceId);
        return traceId;
    }

    /** 获取当前线程的 TraceId */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /** 清理 ThreadLocal，防止内存泄漏（非常重要！） 建议在 Web 拦截器的 afterCompletion 中调用 */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
