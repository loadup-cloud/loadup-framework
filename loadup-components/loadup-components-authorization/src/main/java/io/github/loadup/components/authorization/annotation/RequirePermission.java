package io.github.loadup.components.authorization.annotation;

/*-
 * #%L
 * LoadUp Components Authorization
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for permission-based access control at method or class level.
 *
 * <p>Usage examples:</p>
 * <pre>
 * {@code
 * @RequirePermission("user:delete")
 * public void deleteUser(String userId) { }
 *
 * @RequirePermission(value = {"user:read", "user:write"}, logical = Logical.OR)
 * public void updateUser() { }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * Required permission(s).
     * Format: "resource:action" (e.g., "user:delete", "order:create")
     */
    String[] value();

    /**
     * Logical operator when multiple permissions are specified.
     * Default is OR (any one of the permissions is sufficient)
     */
    Logical logical() default Logical.OR;
}
