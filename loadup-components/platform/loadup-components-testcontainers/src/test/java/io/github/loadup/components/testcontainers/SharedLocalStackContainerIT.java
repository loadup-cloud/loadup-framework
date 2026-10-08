package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.testcontainers.cloud.AbstractLocalStackContainerTest;
import io.github.loadup.components.testcontainers.cloud.SharedLocalStackContainer;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Integration test class for SharedLocalStackContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
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

        LogUtil.info(SharedLocalStackContainerIT.class, "S3 Endpoint: {}", SharedLocalStackContainer.getS3Endpoint());
        LogUtil.info(SharedLocalStackContainerIT.class, "Access Key: {}", SharedLocalStackContainer.getAccessKey());
        LogUtil.info(SharedLocalStackContainerIT.class, "Region: {}", SharedLocalStackContainer.getRegion());
    }

    @Test
    void testS3Connection() {
        try (S3Client s3Client = createS3Client()) {
            // List buckets to verify connection
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            assertNotNull(listBucketsResponse, "List buckets response should not be null");

            LogUtil.info(SharedLocalStackContainerIT.class, "Successfully connected to LocalStack S3");
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
            LogUtil.info(SharedLocalStackContainerIT.class, "Created bucket: {}", bucketName);

            // Verify bucket exists
            HeadBucketRequest headBucketRequest =
                    HeadBucketRequest.builder().bucket(bucketName).build();
            s3Client.headBucket(headBucketRequest);
            LogUtil.info(SharedLocalStackContainerIT.class, "Verified bucket exists: {}", bucketName);

            // List buckets
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            boolean bucketFound = listBucketsResponse.buckets().stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
            assertTrue(bucketFound, "Created bucket should be in the list");

            // Delete bucket
            DeleteBucketRequest deleteBucketRequest =
                    DeleteBucketRequest.builder().bucket(bucketName).build();
            s3Client.deleteBucket(deleteBucketRequest);
            LogUtil.info(SharedLocalStackContainerIT.class, "Deleted bucket: {}", bucketName);
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
            LogUtil.info(SharedLocalStackContainerIT.class, "Uploaded object: {}", objectKey);

            // Get the object
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            String retrievedContent =
                    s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
            assertEquals(content, retrievedContent, "Retrieved content should match uploaded content");
            LogUtil.info(SharedLocalStackContainerIT.class, "Retrieved object content: {}", retrievedContent);

            // Delete the object
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            LogUtil.info(SharedLocalStackContainerIT.class, "Deleted object: {}", objectKey);

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
