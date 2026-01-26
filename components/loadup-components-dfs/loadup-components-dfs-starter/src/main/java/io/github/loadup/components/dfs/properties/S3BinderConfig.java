package io.github.loadup.components.dfs.properties;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class S3BinderConfig extends BinderConfig {

  /** 访问密钥 */
  private String accessKey;

  /** 密钥 */
  private String secretKey;

  /** 区域 */
  private String region;

  /** 存储桶名称 */
  private String bucket;

  /** 端点URL（用于本地测试或自定义S3兼容服务） */
  private String endpoint;
}
