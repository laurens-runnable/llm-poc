package nl.runnable.archeo.report.jpa

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

interface ReportEntityRepository : JpaRepository<ReportEntity, UUID> {
    fun findByWorkflowIdIsNullAndApprovedIsFalse(): List<ReportEntity>

    fun findReport(id: UUID) = findByIdOrNull(id) ?: throw EntityNotFoundException("Report not found: $id")
}
