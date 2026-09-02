package nl.runnable.archeo.document.graphql

import nl.runnable.archeo.document.jpa.DocumentEntity
import org.springframework.data.domain.Page

class DocumentPage(
    val items: List<Document>,
)

fun Page<DocumentEntity>.toDocumentPage() = DocumentPage(content.map { it.toDocument() })
