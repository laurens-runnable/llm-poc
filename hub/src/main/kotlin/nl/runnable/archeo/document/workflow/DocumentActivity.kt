package nl.runnable.archeo.document.workflow

import com.uber.cadence.activity.ActivityMethod
import com.uber.cadence.workflow.Workflow
import jakarta.persistence.EntityNotFoundException
import nl.runnable.archeo.document.NerHelper
import nl.runnable.archeo.document.jpa.DocumentEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

interface DocumentActivity {
    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun extractEntities(id: UUID)

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun saveEntities(
        id: UUID,
        entities: List<Map<String, String>>,
    )

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun approve(id: UUID)
}

@Component
class DocumentActivityImpl(
    private val repository: DocumentEntityRepository,
    private val nerHelper: NerHelper,
) : DocumentActivity {
    override fun extractEntities(id: UUID) {
        nerHelper.extractEntities(id, Workflow.getWorkflowInfo().workflowId)
    }

    override fun saveEntities(
        id: UUID,
        entities: List<Map<String, String>>,
    ) {
        val document = repository.findByIdOrNull(id) ?: throw EntityNotFoundException("Document not found: $id")
        val json = ObjectMapper().writeValueAsString(entities)
        document.namedEntities = json
        repository.save(document)
    }

    override fun approve(id: UUID) {
        val document = repository.findByIdOrNull(id) ?: throw EntityNotFoundException("Document not found: $id")
        document.status = "approved"
        repository.save(document)
    }
}
