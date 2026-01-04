package com.github.loadup.components.database.repository;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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
import org.springframework.data.jdbc.repository.query.Query;

/**
 * Tenant Aware Query annotation
 *
 * <p>A replacement for @Query that automatically adds tenant_id and deleted filters.
 *
 * <p>Before (manual):
 *
 * <pre>
 * &#64;Query("SELECT * FROM upms_user WHERE username = :username AND deleted = false AND tenant_id = :tenantId")
 * Optional&lt;User&gt; findByUsername(String username, String tenantId);
 * </pre>
 *
 * <p>After (automatic):
 *
 * <pre>
 * &#64;TenantQuery("SELECT * FROM upms_user WHERE username = :username")
 * Optional&lt;User&gt; findByUsername(String username);
 * </pre>
 *
 * <p>The annotation processor will automatically append:
 *
 * <ul>
 *   <li>{@code AND tenant_id = <current_tenant>} when multi-tenant is enabled
 *   <li>{@code AND deleted = false} when logical delete is enabled for the tenant
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Query("") // This will be replaced by processor
public @interface TenantQuery {

  /** The SQL query (without tenant_id and deleted filters) */
  String value();

  /** Whether to apply tenant filter (default: true) */
  boolean applyTenantFilter() default true;

  /** Whether to apply logical delete filter (default: true) */
  boolean applyDeletedFilter() default true;
}
