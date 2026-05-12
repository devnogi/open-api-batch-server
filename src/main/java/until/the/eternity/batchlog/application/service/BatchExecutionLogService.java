package until.the.eternity.batchlog.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.domain.enums.TriggerType;
import until.the.eternity.batchlog.domain.repository.BatchExecutionLogRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchExecutionLogService {

    private final BatchExecutionLogRepositoryPort repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccess(
            BatchType batchType,
            TriggerType triggerType,
            LocalDateTime startedAt,
            int recordCount,
            String message) {
        BatchExecutionLog log =
                BatchExecutionLog.builder()
                        .batchType(batchType)
                        .triggerType(triggerType)
                        .startedAt(startedAt)
                        .completedAt(LocalDateTime.now())
                        .isSuccess(true)
                        .recordCount(recordCount)
                        .message(message)
                        .build();
        repository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(
            BatchType batchType, TriggerType triggerType, LocalDateTime startedAt, String message) {
        BatchExecutionLog log =
                BatchExecutionLog.builder()
                        .batchType(batchType)
                        .triggerType(triggerType)
                        .startedAt(startedAt)
                        .completedAt(LocalDateTime.now())
                        .isSuccess(false)
                        .recordCount(0)
                        .message(message)
                        .build();
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<BatchExecutionLog> findByBatchType(BatchType batchType, int page, int size) {
        return repository.findByBatchType(batchType, PageRequest.of(page - 1, size));
    }

    @Transactional(readOnly = true)
    public List<BatchExecutionLog> findLatestPerBatchType() {
        return repository.findLatestPerBatchType();
    }
}
