package io.github.loadup.components.extension.annotation;

import java.lang.annotation.*;

/**
 * 标注在 {@code @Service} 方法参数或方法上，声明该方法需要绑定业务场景上下文。
 *
 * <p>与 {@link BizContextHolder} 和 {@code BizScenarioInterceptor} 配合使用：
 *
 * <ul>
 *   <li>标注在方法上：拦截器自动从参数中解析 {@code bizCode} 字段并写入上下文
 *   <li>标注在参数上：标记该参数为业务场景来源（参数类型必须实现 {@code BizIdentity} 接口）
 * </ul>
 *
 * <p>示例：
 *
 * <pre>{@code
 * @BizScenario
 * public OrderDTO createOrder(OrderCreateCommand cmd) { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface BizScenario {

    /**
     * 指定固定的 bizCode，优先级高于运行时解析。
     *
     * <p>若不指定（空字符串），则拦截器从方法的第一个参数中读取 {@code bizCode} 字段。
     */
    String bizCode() default "";

    /** 指定固定的 useCase，默认 "default"。 */
    String useCase() default "default";

    /** 指定固定的 scenario，默认 "default"。 */
    String scenario() default "default";
}
