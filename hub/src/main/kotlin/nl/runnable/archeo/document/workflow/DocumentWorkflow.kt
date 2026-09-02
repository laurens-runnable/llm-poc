package nl.runnable.archeo.document.workflow

import com.uber.cadence.workflow.SignalMethod
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import org.slf4j.Logger
import java.util.UUID

interface DocumentWorkflow {
    @WorkflowMethod
    fun editMetadata(id: UUID)

    @SignalMethod
    fun setEntities(entities: List<Map<String, String>>)

    @SignalMethod
    fun approve()
}

private val workflowLogger: Logger = Workflow.getLogger(DocumentWorkflowImpl::class.java)

class DocumentWorkflowImpl : DocumentWorkflow {
    private val activity = Workflow.newActivityStub(DocumentActivity::class.java)
    var entities = ArrayList<Map<String, String>>()
    var approved = false

    override fun editMetadata(id: UUID) {
        workflowLogger.info("Document workflow started: {}", id)

        Workflow.await { entities.isNotEmpty() }
        workflowLogger.info("Entities: {}", entities)
        activity.saveEntities(id, entities)

        Workflow.await { approved }
        workflowLogger.info("Document workflow completed: {}", id)
    }

    override fun setEntities(entities: List<Map<String, String>>) {
        this.entities.clear()
        this.entities.addAll(entities)
    }

    override fun approve() {
        approved = true
    }
}
