package nl.runnable.archeo.report.workflow

import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.ChildWorkflowOptions
import com.uber.cadence.workflow.SignalMethod
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import org.slf4j.Logger
import java.time.Duration
import java.util.UUID

private val workflowLogger: Logger = Workflow.getLogger(ReportWorkflowImpl::class.java)

private const val NER_TASK_LIST = "ner-task-list"

interface ReportWorkflow {
    @WorkflowMethod
    fun run(reportId: UUID)

    @SignalMethod
    fun setNamedEntities(entities: List<Map<String, String>>)

    @SignalMethod
    fun approve()
}

class ReportWorkflowImpl : ReportWorkflow {
    val activity = Workflow.newActivityStub(ReportActivity::class.java)!!

    var namedEntities = ArrayList<Map<String, String>>()

    var approved = false

    override fun run(reportId: UUID) {
        workflowLogger.info("Report workflow started: {}", reportId)

        val filename = activity.getFilename(reportId)
        val nerWorkflow =
            Workflow.newChildWorkflowStub(
                NerWorkflow::class.java,
                ChildWorkflowOptions
                    .Builder()
                    .setTaskList(NER_TASK_LIST)
                    .setExecutionStartToCloseTimeout(
                        Duration.ofHours(1),
                    ).build(),
            )

        val workflowId = Workflow.getWorkflowInfo().workflowId

//        Async.procedure(nerWorkflow::run, filename, workflowId)
//        val childExecution = Workflow.getWorkflowExecution(nerWorkflow)
//        childExecution.get()

        activity.extractNamedEntities(reportId, workflowId)
        workflowLogger.info("Awaiting Named Entity Recognition")
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
