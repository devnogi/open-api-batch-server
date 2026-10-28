package until.the.eternity.batchlog.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;

public interface BatchExecutionLogJpaRepository extends JpaRepository<BatchExecutionLog, Long> {

    Page<BatchExecutionLog> findByBatchTypeOrderByStartedAtDesc(
            BatchType batchType, Pageable pageable);

    Optional<BatchExecutionLog> findTopByBatchTypeOrderByStartedAtDesc(BatchType batchType);
}
