package nl.runnable.archeo.report.graphql

import nl.runnable.archeo.report.jpa.ReportEntity
import java.util.UUID

class Report(
    val id: UUID,
    val filename: String,
    val approved: Boolean,
    val workflowActive: Boolean,
    val namedEntities: List<NamedEntity>?,
)

fun ReportEntity.toReport(): Report {
    val entities =
        namedEntities ?.map { (key, values) -> NamedEntity(key, values.toList()) }?.toList()
    return Report(
        id = id,
        filename = filename,
        approved = approved,
        workflowActive = workflowId != null,
        namedEntities = entities,
    )
}
