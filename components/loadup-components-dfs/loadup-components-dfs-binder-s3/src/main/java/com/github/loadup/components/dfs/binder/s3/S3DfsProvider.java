package com.github.loadup.components.dfs.binder.s3;

/*-
 * #%L
 * loadup-components-dfs-binder-s3
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

import com.github.loadup.components.dfs.api.IDfsProvider;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.extension.annotation.Extension;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * S3对象存储提供者
 */
@Slf4j
@Component
@Extension(bizCode = "DFS", useCase = "s3")
public class S3DfsProvider implements IDfsProvider {

    @Autowired
    private DfsProperties dfsProperties;

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private String bucketName;

    private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @PostConstruct
    public void init() {
        DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("s3");
        if (config != null && config.isEnabled()) {
            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(
                        config.getAccessKey(),
                        config.getSecretKey()
                );

                Region region = Region.of(config.getRegion() != null ? config.getRegion() : "us-east-1");

                var clientBuilder = S3Client.builder()
                        .region(region)
                        .credentialsProvider(StaticCredentialsProvider.create(credentials));

                // 如果配置了自定义endpoint (例如MinIO)
                if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
                    clientBuilder.endpointOverride(URI.create(config.getEndpoint()));
                }

                s3Client = clientBuilder.build();

                // 初始化presigner
                var presignerBuilder = S3Presigner.builder()
                        .region(region)
                        .credentialsProvider(StaticCredentialsProvider.create(credentials));

                if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
                    presignerBuilder.endpointOverride(URI.create(config.getEndpoint()));
                }

                s3Presigner = presignerBuilder.build();

                bucketName = config.getBucket();

                // 检查bucket是否存在，如不存在则创建
                ensureBucketExists();

                log.info("S3 provider initialized successfully with bucket: {}", bucketName);

            } catch (Exception e) {
                log.error("Failed to initialize S3 provider: {}", e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
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

            // 构建上传请求
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(request.getContentType())
                    .contentLength((long) content.length)
                    .build();

            // 上传文件
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            log.info("File uploaded successfully to S3: {} -> {}", request.getFilename(), objectKey);

            return FileMetadata.builder()
                    .fileId(fileId)
                    .filename(request.getFilename())
                    .size(size)
                    .contentType(request.getContentType())
                    .provider(getProviderName())
                    .path(objectKey)
                    .hash(hash)
                    .bizType(request.getBizType())
                    .bizId(request.getBizId())
                    .status(FileStatus.AVAILABLE)
                    .publicAccess(request.getPublicAccess())
                    .metadata(request.getMetadata())
                    .uploadTime(LocalDateTime.now())
                    .accessCount(0L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        try {
            FileMetadata metadata = getMetadata(fileId);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(metadata.getPath())
                    .build();

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

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(metadata.getPath())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", fileId);
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

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(metadata.getPath())
                    .build();

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
        // In a real implementation, this would fetch from a database
        // For now, we'll throw an exception as metadata needs to be persisted separately
        throw new UnsupportedOperationException("Metadata retrieval requires database integration");
    }

    @Override
    public String generatePresignedUrl(String fileId, long expirationSeconds) {
        try {
            FileMetadata metadata = getMetadata(fileId);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(metadata.getPath())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
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

            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceMetadata.getPath())
                    .destinationBucket(bucketName)
                    .destinationKey(targetPath)
                    .build();

            s3Client.copyObject(copyObjectRequest);

            String newFileId = UUID.randomUUID().toString();

            return FileMetadata.builder()
                    .fileId(newFileId)
                    .filename(sourceMetadata.getFilename())
                    .size(sourceMetadata.getSize())
                    .contentType(sourceMetadata.getContentType())
                    .provider(getProviderName())
                    .path(targetPath)
                    .hash(sourceMetadata.getHash())
                    .status(FileStatus.AVAILABLE)
                    .uploadTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to copy file in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to copy file", e);
        }
    }

    @Override
    public String getProviderName() {
        return "s3";
    }

    @Override
    public boolean isAvailable() {
        DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("s3");
        return config != null && config.isEnabled() && s3Client != null;
    }

    private void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("Bucket exists: {}", bucketName);

        } catch (NoSuchBucketException e) {
            log.info("Bucket does not exist, creating: {}", bucketName);
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
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
}

