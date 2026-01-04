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

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Base Repository with automatic tenant and logical delete filtering
 *
 * <p>Extends this interface instead of CrudRepository to get automatic filtering:
 *
 * <ul>
 *   <li>Automatic tenant_id filtering (when multi-tenant is enabled)
 *   <li>Automatic deleted = false filtering (when logical delete is enabled)
 * </ul>
 *
 * <p>Example:
 *
 * <pre>
 * public interface UserRepository extends TenantAwareRepository&lt;User, Long&gt; {
 *     // No need to add "AND deleted = false AND tenant_id = ?" manually
 *     Optional&lt;User&gt; findByUsername(String username);
 *
 *     List&lt;User&gt; findByDeptId(String deptId);
 * }
 * </pre>
 *
 * <p>For cross-tenant or include-deleted queries, use @SkipTenantFilter:
 *
 * <pre>
 * &#64;SkipTenantFilter
 * &#64;Query("SELECT * FROM upms_user")
 * List&lt;User&gt; findAllIncludingDeleted();
 * </pre>
 *
 * @param <T> entity type
 * @param <ID> ID type
 * @author LoadUp Framework
 * @since 1.0.0
 */
@NoRepositoryBean
public interface TenantAwareRepository<T, ID>
    extends CrudRepository<T, ID>, PagingAndSortingRepository<T, ID> {

  // Spring Data JDBC will automatically apply tenant and deleted filters
  // through query method name parsing and SQL interception
}
