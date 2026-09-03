package nl.runnable.archeo.report.workflow

import com.uber.cadence.workflow.SignalMethod
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import org.slf4j.Logger
import java.util.UUID

interface ReportWorkflow {
    @WorkflowMethod
    fun editMetadata(reportId: UUID)

    @SignalMethod
    fun setNamedEntities(entities: List<Map<String, String>>)

    @SignalMethod
    fun approve()
}

private val workflowLogger: Logger = Workflow.getLogger(ReportWorkflowImpl::class.java)

class ReportWorkflowImpl : ReportWorkflow {
    val activity = Workflow.newActivityStub(ReportActivity::class.java)!!

    var namedEntities = ArrayList<Map<String, String>>()

    var approved = false

    override fun editMetadata(reportId: UUID) {
        workflowLogger.info("Report workflow started: {}", reportId)

        activity.extractNamedEntities(reportId, Workflow.getWorkflowInfo().workflowId)
        Workflow.await { namedEntities.isNotEmpty() }
        workflowLogger.info("Saving Named Entities: {}", namedEntities)
        activity.saveNamedEntities(reportId, namedEntities)

        Workflow.await { approved }
        activity.approve(reportId)

        workflowLogger.info("Report workflow completed: {}", reportId)
    }

    override fun setNamedEntities(entities: List<Map<String, String>>) {
        namedEntities.clear()
        namedEntities.addAll(entities)
    }

    override fun approve() {
        approved = true
    }
}
