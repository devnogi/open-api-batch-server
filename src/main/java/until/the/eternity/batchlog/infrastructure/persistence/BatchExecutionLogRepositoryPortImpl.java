package until.the.eternity.batchlog.infrastructure.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
    public Page<BatchExecutionLog> findByBatchType(BatchType batchType, Pageable pageable) {
        return jpaRepository.findByBatchTypeOrderByStartedAtDesc(batchType, pageable);
    }

    @Override
    public List<BatchExecutionLog> findLatestPerBatchType() {
        return Arrays.stream(BatchType.values())
                .map(jpaRepository::findTopByBatchTypeOrderByStartedAtDesc)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }
}
