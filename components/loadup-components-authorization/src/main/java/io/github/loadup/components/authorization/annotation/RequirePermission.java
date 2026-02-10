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
