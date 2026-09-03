package nl.runnable.archeo.document.graphql

import nl.runnable.archeo.document.jpa.DocumentEntity
import java.util.UUID

class Document(
    val id: UUID,
    val filename: String,
    val approved: Boolean,
)

fun DocumentEntity.toDocument(): Document = Document(id, filename, approved)
