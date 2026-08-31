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
 * Annotation for role-based access control at method or class level.
 *
 * <p>Usage examples:</p>
 * <pre>
 * {@code
 * @RequireRole("ADMIN")
 * public void deleteUser(String userId) { }
 *
 * @RequireRole(value = {"ADMIN", "USER"}, logical = Logical.OR)
 * public void viewProfile() { }
 *
 * @RequireRole(value = {"ADMIN", "AUDITOR"}, logical = Logical.AND)
 * public void auditAction() { }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * Required role(s).
     * Can be specified with or without "ROLE_" prefix (e.g., "ADMIN" or "ROLE_ADMIN")
     */
    String[] value();

    /**
     * Logical operator when multiple roles are specified.
     * Default is OR (any one of the roles is sufficient)
     */
    Logical logical() default Logical.OR;
}
