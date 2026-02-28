package io.github.loadup.modules.log.client.annotation;

/*-
 * #%L
 * Loadup Modules Log Client
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for automatic operation log recording via AOP.
 *
 * <p>Place on any Spring-managed bean method. The aspect will record the caller,
 * arguments, result, duration and success/failure asynchronously.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** Operation type: CREATE / UPDATE / DELETE / QUERY / EXPORT / LOGIN / LOGOUT */
    String type();

    /** Business module name, e.g. "用户管理" */
    String module() default "";

    /** Human-readable description, e.g. "删除用户" */
    String description() default "";

    /** Whether to serialise method arguments into the log record */
    boolean recordParams() default false;

    /** Whether to serialise the return value into the log record */
    boolean recordResponse() default false;
}
