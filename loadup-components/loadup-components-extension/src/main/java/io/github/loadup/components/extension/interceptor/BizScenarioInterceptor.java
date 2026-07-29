package io.github.loadup.components.extension.interceptor;

/*-
 * #%L
 * Loadup Components Extension
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

import io.github.loadup.components.extension.annotation.BizScenario;
import io.github.loadup.components.extension.api.BizIdentity;
import io.github.loadup.components.extension.context.BizContextHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BizScenarioInterceptor implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(BizScenarioInterceptor.class);

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

    private io.github.loadup.components.extension.core.BizScenario resolveScenario(BizScenario ann, Object... args) {

        // 1. 注解上有固定 bizCode，直接使用
        if (StringUtils.hasText(ann.bizCode())) {
            return new io.github.loadup.components.extension.core.BizScenario(
                    ann.bizCode(), ann.useCase(), ann.scenario());
        }

        // 2. 从第一个实现了 BizIdentity 的参数中动态读取
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof BizIdentity identity) {
                    return new io.github.loadup.components.extension.core.BizScenario(
                            identity.getBizCode(), identity.getUseCase(), identity.getScenario());
                }
            }
        }

        return null;
    }
}
