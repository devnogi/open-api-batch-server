package until.the.eternity.batchlog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;

import java.util.List;

public interface BatchExecutionLogRepositoryPort {

    void save(BatchExecutionLog log);

    Page<BatchExecutionLog> findByBatchType(BatchType batchType, Pageable pageable);

    List<BatchExecutionLog> findLatestPerBatchType();
}
