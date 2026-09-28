package io.github.loadup.components.testcontainers.cloud;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures LocalStack TestContainer for S3.
 *
 * <p>Test classes can extend this class to automatically use the shared LocalStack container
 * without needing to manually configure the context initializer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * class MyS3Test extends AbstractLocalStackContainerTest {
 *     &#64;Autowired
 *     private S3Client s3Client;
 *
 *     &#64;Test
 *     void testS3Upload() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.LOCALSTACK)
public abstract class AbstractLocalStackContainerTest {

    /**
     * Get the S3 endpoint URL.
     *
     * @return the S3 endpoint URL
     */
    protected String getS3Endpoint() {
        return SharedLocalStackContainer.getS3Endpoint();
    }

    /**
     * Get the AWS access key.
     *
     * @return the access key
     */
    protected String getAccessKey() {
        return SharedLocalStackContainer.getAccessKey();
    }

    /**
     * Get the AWS secret key.
     *
     * @return the secret key
     */
    protected String getSecretKey() {
        return SharedLocalStackContainer.getSecretKey();
    }

    /**
     * Get the AWS region.
     *
     * @return the region
     */
    protected String getRegion() {
        return SharedLocalStackContainer.getRegion();
    }
}
