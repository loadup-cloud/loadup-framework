package io.github.loadup.components.extension.context;

import io.github.loadup.components.extension.core.BizScenario;

/**
 * 业务场景上下文持有者（ThreadLocal）。
 *
 * <p>在整个请求链路中透传当前业务场景，避免每层方法都显式传递 {@link BizScenario} 参数。
 *
 * <p>典型用法：
 *
 * <pre>{@code
 * // 在入口层（Gateway Action / Filter）设置
 * BizContextHolder.set(BizScenario.valueOf("trade", "checkout"));
 *
 * // 在 Service / Extension 内获取
 * BizScenario scenario = BizContextHolder.get();
 *
 * // 请求结束后必须清理（建议在 finally 块）
 * BizContextHolder.clear();
 * }</pre>
 */
public final class BizContextHolder {

    private static final ThreadLocal<BizScenario> CONTEXT = new InheritableThreadLocal<>();

    private BizContextHolder() {}

    /** 设置当前线程的业务场景 */
    public static void set(BizScenario scenario) {
        CONTEXT.set(scenario);
    }

    /**
     * 获取当前线程的业务场景。
     *
     * @return 当前场景，若未设置则返回 {@code null}
     */
    public static BizScenario get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前线程的业务场景，若未设置则返回默认场景。
     *
     * @param defaultScenario 默认场景
     * @return 当前场景或默认值
     */
    public static BizScenario getOrDefault(BizScenario defaultScenario) {
        BizScenario scenario = CONTEXT.get();
        return scenario != null ? scenario : defaultScenario;
    }

    /** 清理当前线程的业务场景，防止内存泄漏。 */
    public static void clear() {
        CONTEXT.remove();
    }
}
