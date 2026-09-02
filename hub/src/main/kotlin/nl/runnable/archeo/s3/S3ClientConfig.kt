package nl.runnable.archeo.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class S3ClientConfig {
    @Value($$"${s3.client.endpoint}")
    lateinit var endpoint: String

    @Value($$"${s3.client.access-key-id}")
    lateinit var accessKeyId: String

    @Value($$"${s3.client.access-key-secret}")
    lateinit var accessKeySecret: String

    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.EU_CENTRAL_1)
            .forcePathStyle(true)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        accessKeyId,
                        accessKeySecret,
                    ),
                ),
            ).build()
}
