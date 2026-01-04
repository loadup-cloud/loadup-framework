package com.github.loadup.components.database.interceptor;

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

import com.github.loadup.components.database.config.DatabaseProperties;
import com.github.loadup.components.database.tenant.TenantConfigService;
import com.github.loadup.components.database.tenant.TenantContextHolder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SQL Interceptor for automatic tenant isolation and logical delete filtering
 *
 * <p>This interceptor automatically modifies SQL queries to add:
 *
 * <ul>
 *   <li>tenant_id filter when multi-tenant is enabled
 *   <li>deleted = false filter when logical delete is enabled for the tenant
 * </ul>
 *
 * <p>Usage: Extend your repository from TenantAwareRepository instead of CrudRepository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlInterceptor {

  private final DatabaseProperties databaseProperties;
  private final TenantConfigService tenantConfigService;

  // Tables to ignore (from configuration)
  private Set<String> ignoreTables;

  // SQL patterns
  private static final Pattern SELECT_PATTERN =
      Pattern.compile("SELECT\\s+.*?\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern UPDATE_PATTERN =
      Pattern.compile("UPDATE\\s+(\\w+)\\s+SET", Pattern.CASE_INSENSITIVE);
  private static final Pattern DELETE_PATTERN =
      Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHERE_PATTERN =
      Pattern.compile("\\s+WHERE\\s+", Pattern.CASE_INSENSITIVE);

  /**
   * Intercept and modify SQL query
   *
   * @param sql original SQL
   * @return modified SQL with tenant and logical delete filters
   */
  public String interceptQuery(String sql) {
    if (!databaseProperties.getMultiTenant().isEnabled()) {
      return sql;
    }

    // Get table name
    String tableName = extractTableName(sql);
    if (tableName == null || isIgnoredTable(tableName)) {
      return sql;
    }

    // Get tenant context
    String tenantId = TenantContextHolder.getTenantId();
    if (tenantId == null) {
      tenantId = databaseProperties.getMultiTenant().getDefaultTenantId();
    }

    // Build additional conditions
    StringBuilder additionalConditions = new StringBuilder();

    // Add tenant filter
    additionalConditions.append("tenant_id = '").append(tenantId).append("'");

    // Add logical delete filter if enabled for this tenant
    if (tenantConfigService.isLogicalDeleteEnabled(tenantId)) {
      additionalConditions.append(" AND deleted = false");
    }

    // Modify SQL
    return addConditionsToSql(sql, additionalConditions.toString());
  }

  /** Extract table name from SQL */
  private String extractTableName(String sql) {
    Matcher selectMatcher = SELECT_PATTERN.matcher(sql);
    if (selectMatcher.find()) {
      return selectMatcher.group(1);
    }

    Matcher updateMatcher = UPDATE_PATTERN.matcher(sql);
    if (updateMatcher.find()) {
      return updateMatcher.group(1);
    }

    Matcher deleteMatcher = DELETE_PATTERN.matcher(sql);
    if (deleteMatcher.find()) {
      return deleteMatcher.group(1);
    }

    return null;
  }

  /** Check if table should be ignored */
  private boolean isIgnoredTable(String tableName) {
    if (ignoreTables == null) {
      String ignoreTablesStr = databaseProperties.getMultiTenant().getIgnoreTables();
      ignoreTables = new HashSet<>(Arrays.asList(ignoreTablesStr.split(",")));
    }
    return ignoreTables.contains(tableName.toLowerCase());
  }

  /** Add conditions to SQL WHERE clause */
  private String addConditionsToSql(String sql, String conditions) {
    Matcher whereMatcher = WHERE_PATTERN.matcher(sql);

    if (whereMatcher.find()) {
      // Has WHERE clause, append with AND
      int whereEndPos = whereMatcher.end();
      return sql.substring(0, whereEndPos)
          + "("
          + conditions
          + ") AND ("
          + sql.substring(whereEndPos)
          + ")";
    } else {
      // No WHERE clause, add one
      // Find position to insert WHERE (before ORDER BY, GROUP BY, LIMIT, etc.)
      String upperSql = sql.toUpperCase();
      int insertPos = sql.length();

      for (String keyword : new String[] {"ORDER BY", "GROUP BY", "LIMIT", "HAVING"}) {
        int pos = upperSql.indexOf(keyword);
        if (pos > 0 && pos < insertPos) {
          insertPos = pos;
        }
      }

      return sql.substring(0, insertPos) + " WHERE " + conditions + " " + sql.substring(insertPos);
    }
  }
}
