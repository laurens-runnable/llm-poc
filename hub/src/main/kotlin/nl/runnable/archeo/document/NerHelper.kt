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
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class NerHelper(
    private val repository: DocumentEntityRepository,
    private val s3Client: S3Client,
) {
    @Value($$"${ner.baseUrl}")
    lateinit var baseUrl: String

    @Value($$"${workflow.work-bucket}")
    lateinit var bucket: String

    lateinit var restClient: RestClient

    @PostConstruct
    fun createRestClient() {
        restClient =
            RestClient
                .builder()
                .requestFactory(SimpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build()
    }

    fun extractNamedEntities(
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

        logger.info { "Extracting Named Entities from $entityId" }
        val response =
            restClient
                .post()
                .uri("/ner/{workflowId}", workflowId)
                .contentType(MediaType.APPLICATION_PDF)
                .body(data)
                .retrieve()
                .toBodilessEntity()
        if (response.statusCode != HttpStatus.ACCEPTED) {
            logger.error { "Unexpected response from NER API: ${response.statusCode}" }
        }
    }
}
