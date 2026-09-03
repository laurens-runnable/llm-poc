package nl.runnable.archeo.report.jpa

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.util.UUID

@Entity
@Table(name = "report")
class ReportEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @Column(name = "filename", nullable = false)
    lateinit var filename: String

    @Type(JsonType::class)
    @Column(name = "named_entities", columnDefinition = "jsonb")
    var namedEntities: Map<String, Set<String>>? = null

    @Column(name = "workflow_id")
    var workflowId: String? = null

    @Column(name = "approved", nullable = false)
    var approved: Boolean = false
}
