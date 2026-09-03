package nl.runnable.archeo.report.elasticsearch

import nl.runnable.archeo.report.jpa.ReportEntity
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.UUID

@Document(indexName = "report", createIndex = true)
class ReportDocument {
    @Id
    lateinit var id: UUID

    @Field(type = FieldType.Text)
    lateinit var filename: String
}

fun ReportEntity.toReportDocument(): ReportDocument {
    val doc = ReportDocument()
    doc.id = id
    doc.filename = filename
    return doc
}
