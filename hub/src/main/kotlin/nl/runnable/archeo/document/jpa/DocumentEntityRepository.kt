package nl.runnable.archeo.document.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DocumentEntityRepository : JpaRepository<DocumentEntity, UUID>
