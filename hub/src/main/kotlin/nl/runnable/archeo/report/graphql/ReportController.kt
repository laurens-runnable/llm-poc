package nl.runnable.archeo.report.graphql

import nl.runnable.archeo.report.ReportHelper
import nl.runnable.archeo.report.jpa.ReportEntityRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ReportController(
    private val repository: ReportEntityRepository,
    private val helper: ReportHelper,
) {
    @QueryMapping
    fun getReport(
        @Argument id: UUID,
    ): Report? = repository.findByIdOrNull(id)?.toReport()

    @QueryMapping
    fun listReports(
        @Argument page: Int,
        @Argument size: Int,
    ): ReportPage = repository.findAll(PageRequest.of(page, size)).toReportPage()

    @MutationMapping
    fun acquireReports(): Boolean {
        helper.acquireReports()
        return true
    }

    @MutationMapping
    fun startWorkflows(): Boolean {
        helper.startWorkflows()
        return true
    }

    @MutationMapping
    fun approveReport(
        @Argument id: UUID,
    ): Boolean {
        helper.approveReport(id)
        return true
    }
}
