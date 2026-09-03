package nl.runnable.archeo.document.graphql

import nl.runnable.archeo.document.DocumentHelper
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
    private val helper: DocumentHelper,
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
    fun acquireDocuments(): Boolean {
        helper.acquireDocuments()
        return true
    }

    @MutationMapping
    fun startWorkflows(): Boolean {
        helper.startWorkflows()
        return true
    }

    @MutationMapping
    fun approveDocument(
        @Argument id: UUID,
    ): Boolean {
        helper.approveDocument(id)
        return true
    }
}
