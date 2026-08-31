package io.github.loadup.components.dfs.s3;

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

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3DfsProvider implements DfsProvider {
    private final S3Client s3Client;
    private final String bucket;
    private final Map<String, FileMetadata> metadataIndex = new HashMap<>();

    public S3DfsProvider(S3DfsConfig config) {
        this.bucket = config.getBucket();
        this.s3Client = S3Client.builder()
                .region(Region.of(config.getRegion()))
                .endpointOverride(java.net.URI.create(config.getEndpoint()))
                .build();
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getInputStream().transferTo(baos);
            byte[] bytes = baos.toByteArray();

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileId)
                    .contentType(request.getContentType())
                    .build();
            s3Client.putObject(putReq, RequestBody.fromBytes(bytes));

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId);
            fileMetadata.setFilename(request.getFilename());
            fileMetadata.setSize((long) bytes.length);
            fileMetadata.setContentType(request.getContentType());

            metadataIndex.put(fileId, fileMetadata);
            return fileMetadata;
        } catch (Exception e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        try {
            byte[] bytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileId)
                            .build())
                    .asByteArray();
            FileMetadata meta = metadataIndex.get(fileId);
            return FileDownloadResponse.builder()
                    .inputStream(new ByteArrayInputStream(bytes))
                    .metadata(meta)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("S3 download failed", e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucket).key(fileId).build());
            metadataIndex.remove(fileId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(String fileId) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(fileId).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return metadataIndex.get(fileId);
    }

    @Override
    public String getBinderType() {
        return "s3";
    }
}
