/*
 * #%L
 * Loadup Components Extension
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
package io.github.loadup.components.extension.interceptor;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.extension.annotation.BizScenario;
import io.github.loadup.components.extension.api.BizIdentity;
import io.github.loadup.components.extension.context.BizContextHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

/**
 *
 *
 *
 * <ol>
 * </ol>
 */
public class BizScenarioInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        BizScenario ann = invocation.getMethod().getAnnotation(BizScenario.class);
        if (ann == null) {
            return invocation.proceed();
        }

        io.github.loadup.components.extension.core.BizScenario scenario =
                resolveScenario(ann, invocation.getArguments());
        var previous = BizContextHolder.get();
        boolean contextSet = false;

        if (scenario != null) {
            BizContextHolder.set(scenario);
            contextSet = true;
            LogUtil.debug(
                    BizScenarioInterceptor.class,
                    "BizContext set: {} for method: {}",
                    scenario.getUniqueIdentity(),
                    invocation.getMethod().getName());
        }

        try {
            return invocation.proceed();
        } finally {
            if (contextSet) {
                if (previous == null) {
                    BizContextHolder.clear();
                } else {
                    BizContextHolder.set(previous);
                }
            }
        }
    }

    private io.github.loadup.components.extension.core.BizScenario resolveScenario(BizScenario ann, Object... args) {

        if (StringUtils.hasText(ann.bizCode())) {
            return new io.github.loadup.components.extension.core.BizScenario(
                    ann.bizCode(), ann.useCase(), ann.scenario());
        }

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
