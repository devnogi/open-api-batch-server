package until.the.eternity.batchlog.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.domain.repository.BatchExecutionLogRepositoryPort;

@Repository
@RequiredArgsConstructor
public class BatchExecutionLogRepositoryPortImpl implements BatchExecutionLogRepositoryPort {

    private final BatchExecutionLogJpaRepository jpaRepository;

    @Override
    public void save(BatchExecutionLog log) {
        jpaRepository.save(log);
    }

    @Override
    public Page<BatchExecutionLog> search(
            BatchType batchType, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return jpaRepository.search(batchType, from, to, pageable);
    }
}
