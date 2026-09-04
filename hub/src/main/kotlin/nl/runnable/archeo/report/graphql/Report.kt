package nl.runnable.archeo.report.graphql

import nl.runnable.archeo.report.jpa.ReportEntity
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class Report(
    val id: UUID,
    val filename: String,
    val approved: Boolean,
    val workflowActive: Boolean,
    val namedEntities: List<NamedEntity>?,
    val modifiedAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
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
        createdAt = createdAt.atOffset(ZoneOffset.UTC),
        modifiedAt = modifiedAt.atOffset(ZoneOffset.UTC),
    )
}
