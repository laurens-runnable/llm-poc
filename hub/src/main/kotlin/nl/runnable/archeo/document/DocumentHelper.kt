package nl.runnable.archeo.document

import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityNotFoundException
import nl.runnable.archeo.AsyncExecutors
import nl.runnable.archeo.document.jpa.DocumentEntity
import nl.runnable.archeo.document.jpa.DocumentEntityRepository
import nl.runnable.archeo.document.workflow.DocumentWorkflow
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException
import software.amazon.awssdk.services.s3.model.ListObjectsResponse
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.time.Duration
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class DocumentHelper(
    private val s3Client: S3Client,
    private val repository: DocumentEntityRepository,
    private val workflowClient: WorkflowClient,
) {
    @Value($$"${workflow.source-bucket}")
    lateinit var sourceBucket: String

    @Value($$"${workflow.work-bucket}")
    lateinit var destBucket: String

    @Value($$"${cadence.task-list}")
    lateinit var taskList: String

    @PostConstruct
    fun createBuckets() {
        for (bucket in listOf(sourceBucket, destBucket)) {
            try {
                s3Client.createBucket { it.bucket(bucket) }
                logger.info { "Created bucket '$bucket'" }
            } catch (_: BucketAlreadyExistsException) {
            } catch (_: BucketAlreadyOwnedByYouException) {
            }
        }
    }

    @Async(AsyncExecutors.SINGLE)
    fun acquireDocuments() {
        val response: ListObjectsResponse =
            try {
                s3Client
                    .listObjects { request -> request.bucket(sourceBucket) }
            } catch (_: NoSuchBucketException) {
                logger.warn { "Bucket not found: $sourceBucket" }
                ListObjectsResponse.builder().build()
            }

        val objects =
            response
                .contents()
                .filter { it.key().endsWith(".pdf") }
        if (objects.isEmpty()) {
            return
        }

        logger.info { "Found ${objects.size} PDFs in '$sourceBucket' bucket" }
        for (item in objects) {
            try {
                s3Client.headObject { request -> request.bucket(destBucket).key(item.key()) }
                logger.warn { "Skipping duplicate object '${item.key()}'" }
                continue
            } catch (_: NoSuchKeyException) {
            }

            logger.info { "Copying ${item.key()} from '$sourceBucket' to '$destBucket'" }
            s3Client.copyObject { request ->
                request
                    .sourceBucket(sourceBucket)
                    .sourceKey(item.key())
                    .destinationBucket(destBucket)
                    .destinationKey(item.key())
            }

            logger.info { "Deleting ${item.key()} from '$sourceBucket'" }
            s3Client.deleteObject { request -> request.bucket(sourceBucket).key(item.key()) }
            repository.save(
                DocumentEntity().apply {
                    id = UUID.randomUUID()
                    filename = item.key()
                },
            )
        }
    }

    @Async(AsyncExecutors.SINGLE)
    fun startWorkflows() {
        for (document in repository.findByWorkflowIdIsNullAndApprovedIsFalse()) {
            logger.info { ("Starting DocumentWorkflow::editMetadata for ${document.id}") }

            val options =
                WorkflowOptions
                    .Builder()
                    .setExecutionStartToCloseTimeout(Duration.ofSeconds(3600))
                    .setTaskList(taskList)
                    .build()
            val workflow =
                workflowClient.newWorkflowStub(DocumentWorkflow::class.java, options)
            val execution =
                WorkflowClient.start(workflow::editMetadata, document.id)
            logger.info { "Started workflow ${execution.workflowId}" }

            document.workflowId = execution.workflowId
            repository.saveAndFlush(document)
        }
    }

    fun approveDocument(documentId: UUID) {
        val document =
            repository.findByIdOrNull(documentId) ?: throw EntityNotFoundException("Document not found: $documentId")
        if (document.approved) {
            assert(document.workflowId == null)
            return
        }

        val workflow =
            workflowClient.newWorkflowStub(DocumentWorkflow::class.java, document.workflowId)
        workflow.approve()
    }
}
