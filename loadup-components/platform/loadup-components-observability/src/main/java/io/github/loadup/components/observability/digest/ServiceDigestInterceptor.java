/*
 * #%L
 * LoadUp Observability
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
package io.github.loadup.components.observability.digest;

import com.google.common.base.Stopwatch;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import java.util.concurrent.TimeUnit;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ClassUtils;

/**
 * Logs fixed metadata only; arguments, results and exception messages are never inspected.
 */
final class ServiceDigestInterceptor implements MethodInterceptor {
    private static void writeSummary(MethodInvocation invocation, long elapsedMs, Throwable failure) {
        try {
            Class<?> serviceClass = ClassUtils.getUserClass(invocation.getThis());
            String code = failure instanceof CommonException business
                    ? business.getResultCode().getCode()
                    : failure == null
                            ? CommonResultCodeEnum.SUCCESS.getCode()
                            : CommonResultCodeEnum.SYS_ERROR.getCode();
            LogUtil.info(
                    ServiceDigestInterceptor.class,
                    "service_digest service={} method={} outcome={} code={} elapsedMs={} exceptionType={}",
                    serviceClass.getName(),
                    invocation.getMethod().toGenericString(),
                    failure == null ? "success" : "failure",
                    code,
                    elapsedMs,
                    failure == null ? "none" : failure.getClass().getName());
        } catch (RuntimeException ignored) {
            // A broken logging backend must not replace a business result or exception.
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Throwable failure = null;
        try {
            return invocation.proceed();
        } catch (Throwable thrown) {
            failure = thrown;
            throw thrown;
        } finally {
            stopwatch.stop();
            writeSummary(invocation, stopwatch.elapsed(TimeUnit.MILLISECONDS), failure);
        }
    }
}
