package io.github.loadup.components.extension.api;

/**
 * 业务身份标记接口。
 *
 * <p>Command / Query 对象实现此接口后，{@code BizScenarioInterceptor} 可自动从请求参数中提取
 * {@code bizCode}，无需在 Service 方法中手动调用 {@code BizContextHolder.set()}。
 *
 * <p>示例：
 *
 * <pre>{@code
 * public class OrderCreateCommand implements BizIdentity {
 *     private String bizCode;   // e.g. "retail" / "catering"
 *     private String useCase;   // optional, e.g. "groupBuy"
 *     // ...other fields
 * }
 * }</pre>
 */
public interface BizIdentity {

    /**
     * 业务代码，对应 {@link io.github.loadup.components.extension.core.BizScenario#getBizCode()}。
     *
     * @return bizCode，不允许为 null
     */
    String getBizCode();

    /**
     * 用例标识，默认 "default"。
     *
     * @return useCase
     */
    default String getUseCase() {
        return "default";
    }

    /**
     * 场景标识，默认 "default"。
     *
     * @return scenario
     */
    default String getScenario() {
        return "default";
    }
}
