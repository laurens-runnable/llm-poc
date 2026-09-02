package nl.runnable.archeo.document.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "document")
class DocumentEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @Column(name = "filename", nullable = false)
    lateinit var filename: String

    @Column(name = "status", nullable = true)
    var status: String? = null

    @Column(name = "named_entities", nullable = true, length = 8192)
    var namedEntities: String? = null
}
