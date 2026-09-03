package nl.runnable.archeo.report.graphql

import nl.runnable.archeo.report.jpa.ReportEntity
import org.springframework.data.domain.Page

class ReportPage(
    val items: List<Report>,
    val totalPages: Int,
    val totalElements: Int,
)

fun Page<ReportEntity>.toReportPage() =
    ReportPage(
        items =
            content.map {
                it.toReport()
            },
        totalPages = totalPages,
        totalElements = totalElements.toInt(),
    )
