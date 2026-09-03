package nl.runnable.archeo.document.workflow

import com.uber.cadence.workflow.SignalMethod
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import org.slf4j.Logger
import java.util.UUID

interface DocumentWorkflow {
    @WorkflowMethod
    fun editMetadata(documentId: UUID)

    @SignalMethod
    fun setNamedEntities(entities: List<Map<String, String>>)

    @SignalMethod
    fun approve()
}

private val workflowLogger: Logger = Workflow.getLogger(DocumentWorkflowImpl::class.java)

class DocumentWorkflowImpl : DocumentWorkflow {
    val activity = Workflow.newActivityStub(DocumentActivity::class.java)!!

    var namedEntities = ArrayList<Map<String, String>>()

    var approved = false

    override fun editMetadata(documentId: UUID) {
        workflowLogger.info("Document workflow started: {}", documentId)

        activity.extractNamedEntities(documentId, Workflow.getWorkflowInfo().workflowId)
        Workflow.await { namedEntities.isNotEmpty() }
        workflowLogger.info("Saving Named Entities: {}", namedEntities)
        activity.saveNamedEntities(documentId, namedEntities)

        Workflow.await { approved }
        activity.approve(documentId)

        workflowLogger.info("Document workflow completed: {}", documentId)
    }

    override fun setNamedEntities(entities: List<Map<String, String>>) {
        namedEntities.clear()
        namedEntities.addAll(entities)
    }

    override fun approve() {
        approved = true
    }
}
