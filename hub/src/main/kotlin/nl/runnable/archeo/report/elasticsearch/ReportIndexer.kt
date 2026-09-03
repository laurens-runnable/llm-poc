package nl.runnable.archeo.report.elasticsearch

import nl.runnable.archeo.report.graphql.toReport
import nl.runnable.archeo.report.jpa.ReportEntityRepository
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ReportIndexer(
    private val operations: ElasticsearchOperations,
    private val repository: ReportEntityRepository,
) {
    @Async
    fun indexAllReports() {
        for (report in repository.findAll()) {
            operations.save(report.toReport())
        }
    }

    @Async
    fun indexReport(id: UUID) {
        val document = repository.findReport(id)
        operations.save(document.toReport())
    }
}
