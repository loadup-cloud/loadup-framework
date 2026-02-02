package io.github.loadup.components.dfs.s3.binder;

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

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.commons.util.StringUtils;
import io.github.loadup.commons.util.context.ApplicationContextUtils;
import io.github.loadup.components.dfs.binder.AbstractDfsBinder;
import io.github.loadup.components.dfs.binder.DfsBinder;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.enums.FileStatus;
import io.github.loadup.components.dfs.model.*;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
public class S3DfsBinder
    extends AbstractDfsBinder<S3DfsBinderCfg, DfsBindingCfg>
    implements DfsBinder<S3DfsBinderCfg, DfsBindingCfg>{
  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private String bucketName;

  private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  // .meta 文件目录前缀（类似 local 的实现）
  private static final String METADATA_PREFIX = ".meta/";

  // S3 原生元数据键（用于快速查询基本信息）
  private static final String META_FILE_ID = "file-id";
  private static final String META_FILENAME = "filename";
  private static final String META_BIZ_TYPE = "biz-type";
  private static final String META_BIZ_ID = "biz-id";
  private static final String META_HASH = "hash";
  private static final String META_STATUS = "status";
  private static final String META_PUBLIC_ACCESS = "public-access";
  private static final String META_UPLOAD_TIME = "upload-time";
  private static final String META_UPLOADER = "uploader";
  private static final String META_ACCESS_COUNT = "access-count";
  private static final String META_CUSTOM_PREFIX = "custom-";

    @Override
    protected void afterConfigInjected(String name, S3DfsBinderCfg binderCfg, DfsBindingCfg bindingCfg) {

    try {
      // 获取访问凭证（优先使用配置，否则从环境变量或 AWS 配置获取）
      String accessKey = getAccessKey(this.binderCfg);
      String secretKey = getSecretKey(this.binderCfg);
      if (accessKey == null || secretKey == null) {
        log.warn(
            "S3 access credentials not configured. Please set loadup.dfs.s3.access-key and secret-key, "
                + "or configure spring.cloud.aws.credentials, or set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables");
        return;
      }
      AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
      // 获取区域配置（优先使用配置，否则使用默认值）
      String regionStr = this.binderCfg.getRegion();
      if (regionStr == null || regionStr.isEmpty()) {
        regionStr = System.getenv("AWS_REGION");
      }
      if (regionStr == null || regionStr.isEmpty()) {
        regionStr = "us-east-1"; // 默认区域
      }
      Region region = Region.of(regionStr);
      var clientBuilder =
          S3Client.builder()
              .region(region)
              .credentialsProvider(StaticCredentialsProvider.create(credentials));
      // 如果配置了自定义endpoint (例如MinIO)
      if (this.binderCfg.getEndpoint() != null && !this.binderCfg.getEndpoint().isEmpty()) {
        clientBuilder.endpointOverride(URI.create(this.binderCfg.getEndpoint()));
      }
      s3Client = clientBuilder.build();
      // 初始化presigner
      var presignerBuilder =
          S3Presigner.builder()
              .region(region)
              .credentialsProvider(StaticCredentialsProvider.create(credentials));
      if (this.binderCfg.getEndpoint() != null && !this.binderCfg.getEndpoint().isEmpty()) {
        presignerBuilder.endpointOverride(URI.create(this.binderCfg.getEndpoint()));
      }
      s3Presigner = presignerBuilder.build();
      bucketName = this.binderCfg.getBucket();
      if (bucketName == null || bucketName.isEmpty()) {
        log.warn("S3 bucket name not configured. Please set loadup.dfs.s3.bucket");
        return;
      }
      // 检查bucket是否存在，如不存在则创建
      ensureBucketExists();
      log.info("S3 provider initialized successfully with bucket: {}", bucketName);
    } catch (Exception e) {
      log.error("Failed to initialize S3 provider: {}", e.getMessage(), e);
    }
  }

  @Override
  public String getBinderType() {
    return "s3";
  }

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    try {
      if (s3Client == null) {
        throw new IllegalStateException("S3 client is not initialized");
      }

      // 生成文件ID和对象键
      String fileId = UUID.randomUUID().toString();
      String objectKey = buildObjectKey(request.getBizType(), fileId, request.getFilename());

      // 读取文件内容并计算哈希
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int bytesRead;
      long size = 0;

      try (InputStream inputStream = request.getInputStream()) {
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
          size += bytesRead;
        }
      }

      byte[] content = baos.toByteArray();

      // 计算MD5哈希
      MessageDigest md = MessageDigest.getInstance("MD5");
      String hash = bytesToHex(md.digest(content));

      LocalDateTime uploadTime = LocalDateTime.now();

      // 构建S3对象元数据 (使用S3原生的UserMetadata)
      Map<String, String> s3Metadata = new HashMap<>();
      s3Metadata.put(META_FILE_ID, fileId);
      s3Metadata.put(META_FILENAME, request.getFilename());
      s3Metadata.put(META_HASH, hash);
      s3Metadata.put(META_STATUS, FileStatus.AVAILABLE.name());
      s3Metadata.put(META_UPLOAD_TIME, uploadTime.toString());
      s3Metadata.put(META_ACCESS_COUNT, "0");

      if (request.getBizType() != null) {
        s3Metadata.put(META_BIZ_TYPE, request.getBizType());
      }
      if (request.getBizId() != null) {
        s3Metadata.put(META_BIZ_ID, request.getBizId());
      }
      if (request.getPublicAccess() != null) {
        s3Metadata.put(META_PUBLIC_ACCESS, String.valueOf(request.getPublicAccess()));
      }

      // 添加自定义元数据
      if (request.getMetadata() != null) {
        request
            .getMetadata()
            .forEach((key, value) -> s3Metadata.put(META_CUSTOM_PREFIX + key, value));
      }

      // 构建上传请求，包含元数据
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(objectKey)
              .contentType(request.getContentType())
              .contentLength((long) content.length)
              .metadata(s3Metadata) // S3原生元数据
              .build();

      // 上传文件（S3 Object Metadata 会一起保存）
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

      // 构建完整的文件元数据对象
      FileMetadata fileMetadata =
          FileMetadata.builder()
              .fileId(fileId)
              .filename(request.getFilename())
              .size(size)
              .contentType(request.getContentType())
              .provider(getBinderType())
              .path(objectKey)
              .hash(hash)
              .bizType(request.getBizType())
              .bizId(request.getBizId())
              .status(FileStatus.AVAILABLE)
              .publicAccess(request.getPublicAccess())
              .metadata(request.getMetadata())
              .uploadTime(uploadTime)
              .accessCount(0L)
              .build();

      // 保存 .meta 文件（用于快速通过 fileId 查找 objectKey）
      saveMetadataFile(fileId, fileMetadata);

      log.info(
          "File uploaded successfully to S3: {} -> {}, metadata saved",
          request.getFilename(),
          objectKey);

      return fileMetadata;

    } catch (Exception e) {
      log.error("Failed to upload file to S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  @Override
  public FileDownloadResponse download(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);

      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(metadata.getPath()).build();

      InputStream inputStream = s3Client.getObject(getObjectRequest);

      return FileDownloadResponse.builder()
          .metadata(metadata)
          .inputStream(inputStream)
          .contentLength(metadata.getSize())
          .build();

    } catch (Exception e) {
      log.error("Failed to download file from S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public boolean delete(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);

      // 删除文件对象
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(metadata.getPath()).build();
      s3Client.deleteObject(deleteObjectRequest);

      // 删除 .meta 文件
      String metaKey = METADATA_PREFIX + fileId + ".json";
      DeleteObjectRequest deleteMetaRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(metaKey).build();
      s3Client.deleteObject(deleteMetaRequest);

      log.info("File and metadata deleted successfully from S3: {}", fileId);
      return true;

    } catch (Exception e) {
      log.error("Failed to delete file from S3: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean exists(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);

      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(bucketName).key(metadata.getPath()).build();

      s3Client.headObject(headObjectRequest);
      return true;

    } catch (NoSuchKeyException e) {
      return false;
    } catch (Exception e) {
      log.error("Error checking file existence in S3: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    try {
      // 策略1: 优先从 .meta 文件读取（快速，直接通过 fileId 定位）
      String metaKey = METADATA_PREFIX + fileId + ".json";

      try {
        GetObjectRequest getMetaRequest =
            GetObjectRequest.builder().bucket(bucketName).key(metaKey).build();

        InputStream metaInputStream = s3Client.getObject(getMetaRequest);
        FileMetadata metadata = JsonUtil.fromJson(metaInputStream, FileMetadata.class);
        // objectMapper.readValue(metaInputStream, FileMetadata.class);

        log.debug("Metadata loaded from .meta file for fileId: {}", fileId);
        return metadata;

      } catch (NoSuchKeyException e) {
        // .meta 文件不存在，尝试从 S3 Object Metadata 读取（兼容旧数据或容错）
        log.warn(
            ".meta file not found for fileId: {}, trying to read from S3 object metadata", fileId);
      }

      // 策略2: 从 S3 Object Metadata 读取（需要先找到 objectKey）
      String objectKey = findObjectKeyByFileId(fileId);
      if (objectKey == null) {
        throw new RuntimeException("File not found: " + fileId);
      }

      // 使用HeadObject获取对象元数据（不下载内容，性能更好）
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();

      HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

      // 从S3元数据中提取信息
      Map<String, String> s3Metadata = headObjectResponse.metadata();

      // 提取自定义元数据
      Map<String, String> customMetadata = new HashMap<>();
      s3Metadata.forEach(
          (key, value) -> {
            if (key.startsWith(META_CUSTOM_PREFIX)) {
              customMetadata.put(key.substring(META_CUSTOM_PREFIX.length()), value);
            }
          });

      // 构建FileMetadata
      FileMetadata metadata =
          FileMetadata.builder()
              .fileId(s3Metadata.get(META_FILE_ID))
              .filename(s3Metadata.get(META_FILENAME))
              .size(headObjectResponse.contentLength())
              .contentType(headObjectResponse.contentType())
              .provider(getBinderType())
              .path(objectKey)
              .hash(s3Metadata.get(META_HASH))
              .bizType(s3Metadata.get(META_BIZ_TYPE))
              .bizId(s3Metadata.get(META_BIZ_ID))
              .status(
                  s3Metadata.get(META_STATUS) != null
                      ? FileStatus.valueOf(s3Metadata.get(META_STATUS))
                      : FileStatus.AVAILABLE)
              .publicAccess(
                  s3Metadata.get(META_PUBLIC_ACCESS) != null
                      ? Boolean.parseBoolean(s3Metadata.get(META_PUBLIC_ACCESS))
                      : false)
              .metadata(customMetadata.isEmpty() ? null : customMetadata)
              .uploadTime(
                  s3Metadata.get(META_UPLOAD_TIME) != null
                      ? LocalDateTime.parse(s3Metadata.get(META_UPLOAD_TIME))
                      : null)
              .uploader(s3Metadata.get(META_UPLOADER))
              .lastAccessTime(
                  headObjectResponse.lastModified() != null
                      ? LocalDateTime.ofInstant(
                          headObjectResponse.lastModified(), java.time.ZoneId.systemDefault())
                      : null)
              .accessCount(
                  s3Metadata.get(META_ACCESS_COUNT) != null
                      ? Long.parseLong(s3Metadata.get(META_ACCESS_COUNT))
                      : 0L)
              .build();

      // 重建 .meta 文件（容错机制）
      try {
        saveMetadataFile(fileId, metadata);
        log.info("Rebuilt .meta file for fileId: {}", fileId);
      } catch (Exception ex) {
        log.warn("Failed to rebuild .meta file for fileId: {}", fileId, ex);
      }

      return metadata;

    } catch (NoSuchKeyException e) {
      log.error("File not found: {}", fileId);
      throw new RuntimeException("File not found: " + fileId, e);
    } catch (Exception e) {
      log.error("Failed to get metadata for fileId: {}", fileId, e);
      throw new RuntimeException("Failed to get metadata", e);
    }
  }

  @Override
  public String generatePresignedUrl(String fileId, long expirationSeconds) {
    try {
      FileMetadata metadata = getMetadata(fileId);

      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(metadata.getPath()).build();

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofSeconds(expirationSeconds))
              .getObjectRequest(getObjectRequest)
              .build();

      PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
      return presignedRequest.url().toString();

    } catch (Exception e) {
      log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate presigned URL", e);
    }
  }

  @Override
  public FileMetadata copy(String sourceFileId, String targetPath) {
    try {
      FileMetadata sourceMetadata = getMetadata(sourceFileId);

      String newFileId = UUID.randomUUID().toString();
      LocalDateTime uploadTime = LocalDateTime.now();

      // 构建新的元数据
      Map<String, String> newS3Metadata = new HashMap<>();
      newS3Metadata.put(META_FILE_ID, newFileId);
      newS3Metadata.put(META_FILENAME, sourceMetadata.getFilename());
      newS3Metadata.put(META_HASH, sourceMetadata.getHash());
      newS3Metadata.put(META_STATUS, FileStatus.AVAILABLE.name());
      newS3Metadata.put(META_UPLOAD_TIME, uploadTime.toString());
      newS3Metadata.put(META_ACCESS_COUNT, "0");

      if (sourceMetadata.getBizType() != null) {
        newS3Metadata.put(META_BIZ_TYPE, sourceMetadata.getBizType());
      }
      if (sourceMetadata.getBizId() != null) {
        newS3Metadata.put(META_BIZ_ID, sourceMetadata.getBizId());
      }
      if (sourceMetadata.getPublicAccess() != null) {
        newS3Metadata.put(META_PUBLIC_ACCESS, String.valueOf(sourceMetadata.getPublicAccess()));
      }

      // 添加自定义元数据
      if (sourceMetadata.getMetadata() != null) {
        sourceMetadata
            .getMetadata()
            .forEach((key, value) -> newS3Metadata.put(META_CUSTOM_PREFIX + key, value));
      }

      // 复制对象并更新元数据
      CopyObjectRequest copyObjectRequest =
          CopyObjectRequest.builder()
              .sourceBucket(bucketName)
              .sourceKey(sourceMetadata.getPath())
              .destinationBucket(bucketName)
              .destinationKey(targetPath)
              .metadata(newS3Metadata)
              .metadataDirective(MetadataDirective.REPLACE) // 使用新元数据替换，而不是复制原元数据
              .contentType(sourceMetadata.getContentType())
              .build();

      s3Client.copyObject(copyObjectRequest);

      // 构建新文件的元数据
      FileMetadata newMetadata =
          FileMetadata.builder()
              .fileId(newFileId)
              .filename(sourceMetadata.getFilename())
              .size(sourceMetadata.getSize())
              .contentType(sourceMetadata.getContentType())
              .provider(getBinderType())
              .path(targetPath)
              .hash(sourceMetadata.getHash())
              .bizType(sourceMetadata.getBizType())
              .bizId(sourceMetadata.getBizId())
              .status(FileStatus.AVAILABLE)
              .publicAccess(sourceMetadata.getPublicAccess())
              .metadata(sourceMetadata.getMetadata())
              .uploadTime(uploadTime)
              .accessCount(0L)
              .build();

      // 保存新文件的 .meta 文件
      saveMetadataFile(newFileId, newMetadata);

      return newMetadata;

    } catch (Exception e) {
      log.error("Failed to copy file in S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to copy file", e);
    }
  }



    @Override
  protected void afterDestroy() {
    if (this.s3Client != null) {
      this.s3Client.close();
      log.info("S3DfsBinder [{}]: S3Client 已成功关闭并释放资源", name);
    }
  }

  /**
   * 获取访问密钥
   *
   * <p>优先级：
   *
   * <ol>
   *   <li>loadup.dfs.binders.s3.access-key
   *   <li>spring.cloud.aws.credentials.access-key
   *   <li>AWS_ACCESS_KEY_ID 环境变量
   * </ol>
   */
  private String getAccessKey(S3DfsBinderCfg config) {
    if (StringUtils.isNotBlank(config.getAccessKey())) {
      return config.getAccessKey();
    }

    Environment environment = ApplicationContextUtils.getEnvironment();
    String property = environment.getProperty("spring.cloud.aws.credentials.access-key");
    if (StringUtils.isNotBlank(property)) {
      return property;
    }
    // 尝试从环境变量获取
    String envKey = System.getenv("AWS_ACCESS_KEY_ID");
    if (StringUtils.isNotBlank(envKey)) {
      return envKey;
    }
    return null;
  }

  /**
   * 获取秘密密钥
   *
   * <p>优先级：
   *
   * <ol>
   *   <li>loadup.dfs.binders.s3.secret-key
   *   <li>spring.cloud.aws.credentials.secret-key
   *   <li>AWS_SECRET_ACCESS_KEY 环境变量
   * </ol>
   */
  private String getSecretKey(S3DfsBinderCfg config) {

    if (StringUtils.isNotBlank(config.getSecretKey())) {
      return config.getSecretKey();
    }

    Environment environment = ApplicationContextUtils.getEnvironment();
    String property = environment.getProperty("spring.cloud.aws.credentials.secret-key");
    if (StringUtils.isNotBlank(property)) {
      return property;
    }
    // 尝试从环境变量获取
    String envKey = System.getenv("AWS_SECRET_ACCESS_KEY");
    if (StringUtils.isNotBlank(envKey)) {
      return envKey;
    }
    return null;
  }

  private void ensureBucketExists() {
    try {
      HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
      s3Client.headBucket(headBucketRequest);
      log.info("Bucket exists: {}", bucketName);

    } catch (NoSuchBucketException e) {
      log.info("Bucket does not exist, creating: {}", bucketName);
      CreateBucketRequest createBucketRequest =
          CreateBucketRequest.builder().bucket(bucketName).build();
      s3Client.createBucket(createBucketRequest);
      log.info("Bucket created successfully: {}", bucketName);
    }
  }

  private String buildObjectKey(String bizType, String fileId, String filename) {
    String datePath = LocalDateTime.now().format(PATH_FORMATTER);
    String prefix = bizType != null && !bizType.isEmpty() ? bizType + "/" + datePath : datePath;

    // 保留原始文件扩展名
    String extension = "";
    int lastDot = filename.lastIndexOf('.');
    if (lastDot > 0) {
      extension = filename.substring(lastDot);
    }

    return prefix + "/" + fileId + extension;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  /**
   * 保存元数据到 .meta 文件
   *
   * <p>将完整的元数据以 JSON 格式存储到 S3 的 .meta/ 目录下
   *
   * <p>文件名格式: .meta/{fileId}.json
   *
   * <p>这样可以通过 fileId 直接定位到元数据文件，无需遍历 bucket
   *
   * @param fileId 文件ID
   * @param metadata 文件元数据
   */
  private void saveMetadataFile(String fileId, FileMetadata metadata) {
    try {
      // 将元数据序列化为JSON
      String metadataJson = JsonUtil.toJson(metadata);

      // 保存到S3的.meta目录下
      String metaKey = METADATA_PREFIX + fileId + ".json";

      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(metaKey)
              .contentType("application/json")
              .build();

      s3Client.putObject(
          putObjectRequest,
          RequestBody.fromString(metadataJson, java.nio.charset.StandardCharsets.UTF_8));

      log.debug("Metadata file saved to S3: {}", metaKey);

    } catch (Exception e) {
      log.error("Failed to save metadata file to S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to save metadata file", e);
    }
  }

  /**
   * 通过fileId查找对象键（仅在 .meta 文件不存在时使用）
   *
   * <p>注意：此方法需要遍历bucket中的对象，性能较差，仅用于容错场景
   *
   * <p>正常情况下应该通过 .meta 文件直接获取
   *
   * @param fileId 文件ID
   * @return 对象键，如果未找到返回null
   */
  private String findObjectKeyByFileId(String fileId) {
    try {
      // 列举bucket中的对象
      ListObjectsV2Request listRequest =
          ListObjectsV2Request.builder().bucket(bucketName).maxKeys(1000).build();

      ListObjectsV2Response listResponse;
      do {
        listResponse = s3Client.listObjectsV2(listRequest);

        for (S3Object s3Object : listResponse.contents()) {
          // 获取每个对象的元数据
          try {
            HeadObjectRequest headRequest =
                HeadObjectRequest.builder().bucket(bucketName).key(s3Object.key()).build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            Map<String, String> metadata = headResponse.metadata();

            if (fileId.equals(metadata.get(META_FILE_ID))) {
              return s3Object.key();
            }
          } catch (Exception e) {
            log.debug("Failed to check metadata for object: {}", s3Object.key());
          }
        }

        // 继续下一页
        if (listResponse.isTruncated()) {
          listRequest =
              listRequest.toBuilder()
                  .continuationToken(listResponse.nextContinuationToken())
                  .build();
        }

      } while (listResponse.isTruncated());

      return null;

    } catch (Exception e) {
      log.error("Failed to find object by fileId: {}", fileId, e);
      return null;
    }
  }
}
