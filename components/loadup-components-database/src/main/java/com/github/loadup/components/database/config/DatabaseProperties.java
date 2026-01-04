package com.github.loadup.components.database.config;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for database component. */
@Data
@ConfigurationProperties(prefix = "loadup.database")
public class DatabaseProperties {

  /** ID generation configuration */
  private IdGenerator idGenerator = new IdGenerator();

  /** Sequence configuration */
  private Sequence sequence = new Sequence();

  /** Multi-tenant configuration */
  private MultiTenant multiTenant = new MultiTenant();

  @Data
  public static class IdGenerator {
    /** Enable automatic ID generation for entities extending BaseDO */
    private boolean enabled = true;

    /** Length of generated ID string (only for random strategy) */
    private int length = 20;

    /**
     * ID generation strategy: random, uuid-v4, uuid-v7, snowflake
     *
     * <ul>
     *   <li>random: 随机字符串，长度可配置
     *   <li>uuid-v4: 标准 UUID v4（随机）
     *   <li>uuid-v7: UUID v7（基于时间戳，有序）
     *   <li>snowflake: 雪花算法（分布式唯一ID，数字型）
     * </ul>
     */
    private String strategy = "random";

    /** Whether to keep hyphens in UUID (for uuid-v4 and uuid-v7) */
    private boolean uuidWithHyphens = false;

    /** Snowflake worker ID (0-31, for snowflake strategy) */
    private long snowflakeWorkerId = 0L;

    /** Snowflake datacenter ID (0-31, for snowflake strategy) */
    private long snowflakeDatacenterId = 0L;
  }

  @Data
  public static class Sequence {
    /** Default step for sequence range allocation */
    private Long step = 1000L;

    /** Minimum value for sequences */
    private Long minValue = 0L;

    /** Maximum value for sequences */
    private Long maxValue = Long.MAX_VALUE;
  }

  @Data
  public static class MultiTenant {
    /** Enable multi-tenant feature (default: false) */
    private boolean enabled = false;

    /** Column name for tenant ID (default: tenant_id) */
    private String columnName = "tenant_id";

    /** Ignore tenant filter for these tables (comma separated) */
    private String ignoreTables = "sys_tenant,sys_user,sys_role,sys_permission";

    /** Default tenant ID when not in tenant context */
    private String defaultTenantId = "default";
  }
}
