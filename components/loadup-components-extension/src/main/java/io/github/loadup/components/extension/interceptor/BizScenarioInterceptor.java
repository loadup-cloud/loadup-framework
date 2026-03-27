package io.github.loadup.components.extension.interceptor;

import io.github.loadup.components.extension.annotation.BizScenario;
import io.github.loadup.components.extension.api.BizIdentity;
import io.github.loadup.components.extension.context.BizContextHolder;
import io.github.loadup.components.extension.core.BizScenario.BizScenarioBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

/**
 * 业务场景 AOP 拦截器。
 *
 * <p>在标注了 {@link BizScenario @BizScenario} 的 Service 方法执行前，自动将业务场景注入
 * {@link BizContextHolder}；方法执行结束后（含异常）自动清理，防止 ThreadLocal 泄漏。
 *
 * <p>场景解析优先级：
 *
 * <ol>
 *   <li>注解上有固定 {@code bizCode} → 直接使用注解值
 *   <li>方法第一个参数实现了 {@link BizIdentity} → 从参数动态读取
 *   <li>以上都不满足 → 跳过（不覆盖已有上下文）
 * </ol>
 */
@Slf4j
public class BizScenarioInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        BizScenario ann = invocation.getMethod().getAnnotation(BizScenario.class);
        if (ann == null) {
            return invocation.proceed();
        }

        io.github.loadup.components.extension.core.BizScenario scenario =
                resolveScenario(ann, invocation.getArguments());
        boolean contextSet = false;

        if (scenario != null) {
            BizContextHolder.set(scenario);
            contextSet = true;
            log.debug(
                    "BizContext set: {} for method: {}",
                    scenario.getUniqueIdentity(),
                    invocation.getMethod().getName());
        }

        try {
            return invocation.proceed();
        } finally {
            if (contextSet) {
                BizContextHolder.clear();
            }
        }
    }

    private io.github.loadup.components.extension.core.BizScenario resolveScenario(BizScenario ann, Object[] args) {

        // 1. 注解上有固定 bizCode，直接使用
        if (StringUtils.hasText(ann.bizCode())) {
            return io.github.loadup.components.extension.core.BizScenario.builder()
                    .bizCode(ann.bizCode())
                    .useCase(ann.useCase())
                    .scenario(ann.scenario())
                    .build();
        }

        // 2. 从第一个实现了 BizIdentity 的参数中动态读取
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof BizIdentity identity) {
                    BizScenarioBuilder builder = io.github.loadup.components.extension.core.BizScenario.builder()
                            .bizCode(identity.getBizCode())
                            .useCase(identity.getUseCase())
                            .scenario(identity.getScenario());
                    return builder.build();
                }
            }
        }

        return null;
    }
}
