package nl.runnable.archeo.report.workflow

import com.uber.cadence.activity.ActivityMethod
import nl.runnable.archeo.report.NerHelper
import nl.runnable.archeo.report.jpa.ReportEntityRepository
import org.springframework.stereotype.Component
import java.util.UUID

interface ReportActivity {
    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun getFilename(reportId: UUID): String

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun extractNamedEntities(
        id: UUID,
        workflowId: String,
    )

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun saveNamedEntities(
        reportId: UUID,
        entities: List<Map<String, String>>,
    )

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 10)
    fun approve(documentId: UUID)
}

@Component
class ReportActivityImpl(
    private val repository: ReportEntityRepository,
    private val nerHelper: NerHelper,
) : ReportActivity {
    override fun getFilename(reportId: UUID) = repository.findReport(reportId).filename

    override fun extractNamedEntities(
        id: UUID,
        workflowId: String,
    ) {
        nerHelper.extractNamedEntities(id, workflowId)
    }

    override fun saveNamedEntities(
        reportId: UUID,
        entities: List<Map<String, String>>,
    ) {
        val namedEntities = LinkedHashMap<String, LinkedHashSet<String>>()
        entities.forEach { item ->
            item.forEach { (key, value) ->
                if (!namedEntities.containsKey(key)) {
                    namedEntities[key] = LinkedHashSet()
                }
                namedEntities[key]?.add(value)
            }
        }

        val document = repository.findReport(reportId)
        document.namedEntities = namedEntities
        repository.save(document)
    }

    override fun approve(documentId: UUID) {
        val document = repository.findReport(documentId)
        checkNotNull(document.namedEntities)
        check(!document.approved)
        document.approved = true
        document.workflowId = null
        repository.save(document)
    }
}
