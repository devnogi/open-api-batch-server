package until.the.eternity.batchlog.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;

public interface BatchExecutionLogJpaRepository extends JpaRepository<BatchExecutionLog, Long> {

    @Query(
            """
            SELECT b FROM BatchExecutionLog b
            WHERE (:batchType IS NULL OR b.batchType = :batchType)
              AND (:from IS NULL OR b.startedAt >= :from)
              AND (:to IS NULL OR b.startedAt < :to)
            ORDER BY b.startedAt DESC
            """)
    Page<BatchExecutionLog> search(
            @Param("batchType") BatchType batchType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
