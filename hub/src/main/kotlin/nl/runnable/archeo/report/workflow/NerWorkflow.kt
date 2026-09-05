package nl.runnable.archeo.report.workflow

import com.uber.cadence.workflow.WorkflowMethod

interface NerWorkflow {
    @WorkflowMethod(name = "NerWorkflow")
    fun run(
        filename: String,
        workflowId: String,
    )
}
