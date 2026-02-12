package io.github.loadup.components.dfs.s3.cfg;

/*-
 * #%L
 * Loadup Dfs Binder S3
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

import io.github.loadup.components.dfs.cfg.DfsBinderCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class S3DfsBinderCfg extends DfsBinderCfg {
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
