package com.github.loadup.components.dfs.config;

/*-
 * #%L
 * loadup-components-dfs-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import com.github.loadup.components.dfs.enums.DfsProviderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * DFS配置属性
 *
 * <p>简化配置方式：
 *
 * <ul>
 *   <li>通过 provider 属性直接指定使用的存储类型（local/database/s3）
 *   <li>配置了哪个 provider 的属性，就使用哪个，不需要 enabled 开关
 *   <li>不同 provider 有独立的配置类，支持 IDE 自动提示
 *   <li>可以复用 Spring Boot 已有配置（DataSource、AWS 配置等）
 * </ul>
 *
 * <p>配置示例：
 *
 * <pre>
 * loadup:
 *   dfs:
 *     provider: s3  # 使用 S3 存储
 *     max-file-size: 104857600
 *     s3:
 *       bucket: my-bucket
 *       # access-key 和 secret-key 可以从 spring.cloud.aws 复用
 *       # region 可以从 spring.cloud.aws.region.static 复用
 * </pre>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsProperties {

  /**
   * 存储提供者类型：local, database, s3
   *
   * <p>配置示例：
   *
   * <pre>
   * loadup.dfs.provider=local     # 使用本地文件系统
   * loadup.dfs.provider=database  # 使用数据库存储
   * loadup.dfs.provider=s3        # 使用 S3 对象存储
   * </pre>
   */
  @NotNull(message = "Provider type must be specified")
  private DfsProviderType provider = DfsProviderType.LOCAL;

    /**
     * 最大文件大小（字节）
     */
    @NotNull(message = "Maximum file size cannot be null")
    @Min(value = 1, message = "Maximum file size must be at least 1 byte")
    private Long maxFileSize = 100 * 1024 * 1024L; // 100MB

    /**
     * 允许的文件类型（MIME类型）
     */
    private String[] allowedContentTypes;

  /** 本地存储配置 */
  @NestedConfigurationProperty private LocalConfig local = new LocalConfig();

  /** 数据库存储配置（复用 Spring DataSource） */
  @NestedConfigurationProperty private DatabaseConfig database = new DatabaseConfig();

  /** S3 对象存储配置（可复用 AWS 配置） */
  @NestedConfigurationProperty private S3Config s3 = new S3Config();

  /** 本地文件系统存储配置 */
  @Data
  public static class LocalConfig {
    /**
     * 文件存储基础路径
     *
     * <p>默认：${user.home}/dfs-storage
     */
    private String basePath = System.getProperty("user.home") + "/dfs-storage";
  }

  /**
   * 数据库存储配置
   *
   * <p>会自动使用 Spring Boot 配置的 DataSource，无需额外配置连接信息
   */
  @Data
  public static class DatabaseConfig {
    /** 数据库表名前缀 */
    private String tablePrefix = "dfs_";
  }

  /**
   * S3 对象存储配置
   *
   * <p>优先使用本配置，如果未配置则尝试从以下位置获取：
   *
   * <ul>
   *   <li>spring.cloud.aws.credentials.access-key
   *   <li>spring.cloud.aws.credentials.secret-key
   *   <li>spring.cloud.aws.region.static
   *   <li>aws.accessKeyId 和 aws.secretAccessKey（环境变量）
   * </ul>
   */
  @Data
  public static class S3Config {
    /** S3 存储桶名称（必填） */
    private String bucket;

    /** S3 访问密钥（可选，未配置时从 AWS 配置或环境变量获取） */
    private String accessKey;

    /** S3 秘密密钥（可选，未配置时从 AWS 配置或环境变量获取） */
    private String secretKey;

    /** AWS 区域（可选，未配置时从 AWS 配置获取，默认：us-east-1） */
    private String region;

    /**
     * 自定义 S3 端点（可选，用于兼容 MinIO 等 S3 兼容存储）
     *
     * <p>示例：
     *
     * <ul>
     *   <li>MinIO: http://localhost:9000
     *   <li>阿里云 OSS: https://oss-cn-hangzhou.aliyuncs.com
     * </ul>
     */
    private String endpoint;
    }
}

