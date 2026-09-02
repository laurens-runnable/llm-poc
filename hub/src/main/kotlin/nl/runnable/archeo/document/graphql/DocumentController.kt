package nl.runnable.archeo.document.graphql

import nl.runnable.archeo.document.DocumentSyncHelper
import nl.runnable.archeo.document.jpa.DocumentEntityRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class DocumentController(
    private val repository: DocumentEntityRepository,
    private val helper: DocumentSyncHelper,
) {
    @QueryMapping
    fun getDocument(
        @Argument id: UUID,
    ): Document? = repository.findByIdOrNull(id)?.toDocument()

    @QueryMapping
    fun listDocuments(
        @Argument page: Int,
        @Argument size: Int,
    ): DocumentPage = repository.findAll(PageRequest.of(page, size)).toDocumentPage()

    @MutationMapping
    fun syncInbox(): Boolean {
        helper.syncInbox()
        return true
    }
}
