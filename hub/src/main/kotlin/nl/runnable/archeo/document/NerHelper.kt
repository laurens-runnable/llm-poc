package nl.runnable.archeo.document

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityNotFoundException
import nl.runnable.archeo.document.jpa.DocumentEntityRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class NerHelper(
    private val repository: DocumentEntityRepository,
    private val s3Client: S3Client,
) {
    @Value($$"${ner.baseUrl}")
    lateinit var baseUrl: String

    lateinit var restClient: RestClient

    @Value($$"${workflow.work-bucket}")
    lateinit var bucket: String

    @PostConstruct
    fun createRestClient() {
        restClient =
            RestClient
                .builder()
                .requestFactory(SimpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build()
    }

    fun extractEntities(
        entityId: UUID,
        workflowId: String,
    ) {
        val document =
            repository.findByIdOrNull(entityId) ?: throw EntityNotFoundException("Document not found: $entityId")
        val data =
            try {
                s3Client.getObjectAsBytes { request -> request.bucket(bucket).key(document.filename) }.asByteArray()
            } catch (_: NoSuchKeyException) {
                logger.error { "Object '${document.filename} not found in bucket '$bucket'" }
                throw NoSuchElementException("Object not found: ${document.filename}")
            }

        logger.info { "Extracting entities from $entityId" }
        val response =
            restClient
                .post()
                .uri("/ner/{id}", workflowId)
                .contentType(MediaType.APPLICATION_PDF)
                .body(data)
                .retrieve()
                .toBodilessEntity()
        if (response.statusCode != HttpStatus.NO_CONTENT) {
            logger.error { "Unexpected response: " }
        }
    }
}
