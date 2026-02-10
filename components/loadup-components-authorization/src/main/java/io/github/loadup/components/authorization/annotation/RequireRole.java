package io.github.loadup.components.authorization.annotation;

/*-
 * #%L
 * LoadUp Components Authorization
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

import java.lang.annotation.*;

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
