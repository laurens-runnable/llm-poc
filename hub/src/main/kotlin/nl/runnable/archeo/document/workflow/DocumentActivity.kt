package nl.runnable.archeo.document.workflow

import com.uber.cadence.activity.ActivityMethod
import jakarta.persistence.EntityNotFoundException
import nl.runnable.archeo.document.NerHelper
import nl.runnable.archeo.document.jpa.DocumentEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

interface DocumentActivity {
    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun extractNamedEntities(
        id: UUID,
        workflowId: String,
    )

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun saveNamedEntities(
        documentId: UUID,
        entities: List<Map<String, String>>,
    )

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun approve(documentId: UUID)
}

@Component
class DocumentActivityImpl(
    private val repository: DocumentEntityRepository,
    private val nerHelper: NerHelper,
) : DocumentActivity {
    override fun extractNamedEntities(
        id: UUID,
        workflowId: String,
    ) {
        nerHelper.extractNamedEntities(id, workflowId)
    }

    override fun saveNamedEntities(
        documentId: UUID,
        entities: List<Map<String, String>>,
    ) {
        val document =
            repository.findByIdOrNull(documentId) ?: throw EntityNotFoundException("Document not found: $documentId")
        val json = ObjectMapper().writeValueAsString(entities)
        document.namedEntities = json
        repository.save(document)
    }

    override fun approve(documentId: UUID) {
        val document =
            repository.findByIdOrNull(documentId) ?: throw EntityNotFoundException("Document not found: $documentId")
        document.approved = true
        document.workflowId = null
        repository.save(document)
    }
}
