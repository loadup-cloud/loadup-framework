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

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DFS配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsProperties {

    /**
     * 默认存储提供者
     */
    private String defaultProvider = "local";

    /**
     * 提供者配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 100 * 1024 * 1024L; // 100MB

    /**
     * 允许的文件类型（MIME类型）
     */
    private String[] allowedContentTypes;

    /**
     * 提供者配置
     */
    @Data
    public static class ProviderConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 基础路径（本地存储使用）
         */
        private String basePath;

        /**
         * 端点URL（S3使用）
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 秘密密钥
         */
        private String secretKey;

        /**
         * 存储桶名称（S3使用）
         */
        private String bucket;

        /**
         * 区域（S3使用）
         */
        private String region;

        /**
         * 其他配置
         */
        private Map<String, String> properties = new HashMap<>();
    }
}

