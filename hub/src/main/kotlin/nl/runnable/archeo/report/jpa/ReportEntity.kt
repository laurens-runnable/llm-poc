package nl.runnable.archeo.report.jpa

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener::class)
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

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    lateinit var modifiedAt: Instant
}
