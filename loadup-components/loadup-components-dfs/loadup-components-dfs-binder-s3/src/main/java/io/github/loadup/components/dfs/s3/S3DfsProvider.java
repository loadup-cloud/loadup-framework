/*-
 * #%L
 * Loadup Dfs Binder S3
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.dfs.s3;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.model.MultipartPart;
import io.github.loadup.components.dfs.model.MultipartUpload;
import io.github.loadup.components.dfs.model.MultipartUploadRequest;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/** Thin adapter over the AWS SDK S3 client. */
public class S3DfsProvider implements DfsProvider {
    private static final String META_FILENAME = "loadup-filename";
    private static final String META_UPLOADED_AT = "loadup-uploaded-at";
    private static final Duration MAX_PRESIGN_DURATION = Duration.ofDays(7);

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final String keyPrefix;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Invalid bucket configuration must fail before the provider is published as a bean.")
    public S3DfsProvider(S3Client s3Client, S3Presigner presigner, S3DfsProperties properties) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = requireText(properties.getBucket(), "loadup.dfs.binder.s3.bucket");
        this.keyPrefix = normalizePrefix(properties.getKeyPrefix());
        if (properties.isCreateBucket()) {
            createBucketIfMissing();
        }
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = createFileId(request.path());
        Instant uploadedAt = Instant.now();
        Map<String, String> metadata = toS3Metadata(request.filename(), uploadedAt, request.metadata());
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .contentLength(request.contentLength())
                .contentType(request.contentType())
                .metadata(metadata)
                .build();
        try {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(request.content(), request.contentLength()));
            return new FileMetadata(
                    fileId,
                    request.filename(),
                    request.contentLength(),
                    request.contentType(),
                    getBinderType(),
                    fileId,
                    request.metadata(),
                    uploadedAt);
        } catch (RuntimeException e) {
            throw storageFailure("upload", fileId, e);
        }
    }

    @Override
    @SuppressFBWarnings(
            value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "The response transfers the AWS response stream to the DfsService caller.")
    public FileDownloadResponse download(String fileId) {
        try {
            ResponseInputStream<GetObjectResponse> content = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(fileId).build());
            GetObjectResponse response = content.response();
            FileMetadata metadata = toMetadata(
                    fileId,
                    response.contentLength(),
                    response.contentType(),
                    response.metadata(),
                    response.lastModified());
            return new FileDownloadResponse(metadata, content, response.contentLength());
        } catch (RuntimeException e) {
            throw translate("download", fileId, e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        if (!exists(fileId)) {
            return false;
        }
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucket).key(fileId).build());
            return true;
        } catch (RuntimeException e) {
            throw storageFailure("delete", fileId, e);
        }
    }

    @Override
    public boolean exists(String fileId) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(fileId).build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw storageFailure("exists", fileId, e);
        }
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(fileId).build());
            return toMetadata(
                    fileId,
                    response.contentLength(),
                    response.contentType(),
                    response.metadata(),
                    response.lastModified());
        } catch (RuntimeException e) {
            throw translate("metadata", fileId, e);
        }
    }

    @Override
    public URI generatePresignedDownloadUrl(String fileId, Duration expiration) {
        if (expiration == null
                || expiration.isZero()
                || expiration.isNegative()
                || expiration.compareTo(MAX_PRESIGN_DURATION) > 0) {
            throw new IllegalArgumentException("expiration must be between 1 second and 7 days");
        }
        if (!exists(fileId)) {
            throw new DfsObjectNotFoundException(fileId);
        }
        GetObjectRequest getRequest =
                GetObjectRequest.builder().bucket(bucket).key(fileId).build();
        return URI.create(presigner
                .presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(getRequest)
                        .build())
                .url()
                .toString());
    }

    @Override
    public MultipartUpload initiateMultipartUpload(MultipartUploadRequest request) {
        String fileId = createFileId(request.path());
        Instant uploadedAt = Instant.now();
        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .contentType(request.contentType())
                .metadata(toS3Metadata(request.filename(), uploadedAt, request.metadata()))
                .build();
        try {
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
            return new MultipartUpload(fileId, response.uploadId());
        } catch (RuntimeException e) {
            throw storageFailure("initiate multipart upload", fileId, e);
        }
    }

    @Override
    public MultipartPart uploadPart(
            String fileId, String uploadId, int partNumber, InputStream content, long contentLength) {
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength must not be negative");
        }
        UploadPartRequest uploadRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength(contentLength)
                .build();
        try {
            UploadPartResponse response =
                    s3Client.uploadPart(uploadRequest, RequestBody.fromInputStream(content, contentLength));
            return new MultipartPart(partNumber, response.eTag());
        } catch (RuntimeException e) {
            throw storageFailure("upload multipart part", fileId, e);
        }
    }

    @Override
    public FileMetadata completeMultipartUpload(String fileId, String uploadId, List<MultipartPart> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("parts must not be empty");
        }
        List<CompletedPart> completedParts = parts.stream()
                .sorted(Comparator.comparingInt(MultipartPart::partNumber))
                .map(part -> CompletedPart.builder()
                        .partNumber(part.partNumber())
                        .eTag(part.eTag())
                        .build())
                .toList();
        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .uploadId(uploadId)
                .multipartUpload(
                        CompletedMultipartUpload.builder().parts(completedParts).build())
                .build();
        try {
            s3Client.completeMultipartUpload(completeRequest);
            return getMetadata(fileId);
        } catch (RuntimeException e) {
            throw storageFailure("complete multipart upload", fileId, e);
        }
    }

    @Override
    public void abortMultipartUpload(String fileId, String uploadId) {
        try {
            s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(fileId)
                    .uploadId(uploadId)
                    .build());
        } catch (RuntimeException e) {
            throw storageFailure("abort multipart upload", fileId, e);
        }
    }

    @Override
    public String getBinderType() {
        return "s3";
    }

    private void createBucketIfMissing() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw storageFailure("check bucket", bucket, e);
            }
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private FileMetadata toMetadata(
            String fileId, long size, String contentType, Map<String, String> storedMetadata, Instant lastModified) {
        Map<String, String> customMetadata = new HashMap<>(storedMetadata);
        String filename = customMetadata.remove(META_FILENAME);
        String uploadedAtValue = customMetadata.remove(META_UPLOADED_AT);
        Instant uploadedAt = uploadedAtValue == null ? lastModified : Instant.parse(uploadedAtValue);
        return new FileMetadata(
                fileId,
                filename == null ? fileId.substring(fileId.lastIndexOf('/') + 1) : filename,
                size,
                contentType == null ? "application/octet-stream" : contentType,
                getBinderType(),
                fileId,
                customMetadata,
                uploadedAt);
    }

    private static Map<String, String> toS3Metadata(
            String filename, Instant uploadedAt, Map<String, String> customMetadata) {
        Map<String, String> result = new HashMap<>(customMetadata);
        result.put(META_FILENAME, filename);
        result.put(META_UPLOADED_AT, uploadedAt.toString());
        return result;
    }

    private String createFileId(String requestedPath) {
        List<String> segments = new ArrayList<>();
        if (!keyPrefix.isBlank()) {
            segments.add(keyPrefix);
        }
        if (requestedPath != null && !requestedPath.isBlank()) {
            String normalized = normalizePrefix(requestedPath);
            if (normalized.equals("..") || normalized.startsWith("../") || normalized.contains("/../")) {
                throw new IllegalArgumentException("Invalid S3 object path: " + requestedPath);
            }
            if (!normalized.isBlank()) {
                segments.add(normalized);
            }
        }
        segments.add(UUID.randomUUID().toString().replace("-", ""));
        return String.join("/", segments);
    }

    private static String normalizePrefix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace('\\', '/').replaceAll("^/+|/+$", "");
    }

    private static String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(propertyName + " must not be blank");
        }
        return value;
    }

    private static RuntimeException translate(String operation, String fileId, RuntimeException error) {
        if (error instanceof S3Exception s3Exception && s3Exception.statusCode() == 404) {
            return new DfsObjectNotFoundException(fileId);
        }
        return storageFailure(operation, fileId, error);
    }

    private static DfsStorageException storageFailure(String operation, String fileId, RuntimeException error) {
        return new DfsStorageException("S3 DFS " + operation + " failed for " + fileId, error);
    }
}
