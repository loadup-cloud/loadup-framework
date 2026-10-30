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

import io.github.loadup.commons.log.annotation.ServiceDigest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.util.ReflectionUtils;

/**
 * Selects only explicitly annotated, public business methods.
 */
final class ServiceDigestPointcut extends StaticMethodMatcherPointcut {
    private final AnnotationClassFilter classes = new AnnotationClassFilter(ServiceDigest.class, true);
    private final AnnotationMethodMatcher methods = new AnnotationMethodMatcher(ServiceDigest.class, true);

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return Modifier.isPublic(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && !ReflectionUtils.isObjectMethod(method)
                && (classes.matches(targetClass) || methods.matches(method, targetClass));
    }
}
