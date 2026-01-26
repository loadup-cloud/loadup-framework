package io.github.loadup.framework.api.util;

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
