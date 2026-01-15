package com.github.loadup.components.dfs.s3.cfg;

import com.github.loadup.components.dfs.cfg.DfsBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
