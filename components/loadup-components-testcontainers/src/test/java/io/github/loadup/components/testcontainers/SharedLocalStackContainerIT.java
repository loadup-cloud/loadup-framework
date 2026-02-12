package io.github.loadup.components.testcontainers;

/*-
 * #%L
 * Loadup Components TestContainers
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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.testcontainers.cloud.AbstractLocalStackContainerTest;
import io.github.loadup.components.testcontainers.cloud.SharedLocalStackContainer;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Integration test class for SharedLocalStackContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
        properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.localstack.enabled=true"})
class SharedLocalStackContainerIT extends AbstractLocalStackContainerTest {

    @Test
    void testContainerIsRunning() {
        LocalStackContainer container = SharedLocalStackContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedLocalStackContainer.getS3Endpoint(), "S3 endpoint should not be null");
        assertNotNull(SharedLocalStackContainer.getAccessKey(), "Access key should not be null");
        assertNotNull(SharedLocalStackContainer.getSecretKey(), "Secret key should not be null");
        assertNotNull(SharedLocalStackContainer.getRegion(), "Region should not be null");

        log.info("S3 Endpoint: {}", SharedLocalStackContainer.getS3Endpoint());
        log.info("Access Key: {}", SharedLocalStackContainer.getAccessKey());
        log.info("Region: {}", SharedLocalStackContainer.getRegion());
    }

    @Test
    void testS3Connection() {
        try (S3Client s3Client = createS3Client()) {
            // List buckets to verify connection
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            assertNotNull(listBucketsResponse, "List buckets response should not be null");

            log.info("Successfully connected to LocalStack S3");
        }
    }

    @Test
    void testS3BucketOperations() {
        String bucketName = "test-bucket-" + System.currentTimeMillis();

        try (S3Client s3Client = createS3Client()) {
            // Create a bucket
            CreateBucketRequest createBucketRequest =
                    CreateBucketRequest.builder().bucket(bucketName).build();
            s3Client.createBucket(createBucketRequest);
            log.info("Created bucket: {}", bucketName);

            // Verify bucket exists
            HeadBucketRequest headBucketRequest =
                    HeadBucketRequest.builder().bucket(bucketName).build();
            s3Client.headBucket(headBucketRequest);
            log.info("Verified bucket exists: {}", bucketName);

            // List buckets
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            boolean bucketFound = listBucketsResponse.buckets().stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
            assertTrue(bucketFound, "Created bucket should be in the list");

            // Delete bucket
            DeleteBucketRequest deleteBucketRequest =
                    DeleteBucketRequest.builder().bucket(bucketName).build();
            s3Client.deleteBucket(deleteBucketRequest);
            log.info("Deleted bucket: {}", bucketName);
        }
    }

    @Test
    void testS3ObjectOperations() {
        String bucketName = "test-bucket-" + System.currentTimeMillis();
        String objectKey = "test-object.txt";
        String content = "Hello, LocalStack!";

        try (S3Client s3Client = createS3Client()) {
            // Create a bucket
            s3Client.createBucket(
                    CreateBucketRequest.builder().bucket(bucketName).build());

            // Put an object
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromString(content));
            log.info("Uploaded object: {}", objectKey);

            // Get the object
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            String retrievedContent =
                    s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
            assertEquals(content, retrievedContent, "Retrieved content should match uploaded content");
            log.info("Retrieved object content: {}", retrievedContent);

            // Delete the object
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted object: {}", objectKey);

            // Delete bucket
            s3Client.deleteBucket(
                    DeleteBucketRequest.builder().bucket(bucketName).build());
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        LocalStackContainer container1 = SharedLocalStackContainer.getInstance();
        LocalStackContainer container2 = SharedLocalStackContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }

    /**
     * Creates an S3 client configured to connect to the LocalStack container.
     *
     * @return configured S3 client
     */
    private S3Client createS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(SharedLocalStackContainer.getS3Endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        SharedLocalStackContainer.getAccessKey(), SharedLocalStackContainer.getSecretKey())))
                .region(Region.of(SharedLocalStackContainer.getRegion()))
                .build();
    }
}
