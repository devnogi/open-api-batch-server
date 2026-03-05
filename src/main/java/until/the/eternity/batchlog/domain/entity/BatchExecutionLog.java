package until.the.eternity.batchlog.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.domain.enums.TriggerType;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_execution_log")
@Getter
@NoArgsConstructor
public class BatchExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_type", nullable = false, length = 50)
    private BatchType batchType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    @Column(name = "record_count", nullable = false)
    private int recordCount;

    @Column(name = "message", length = 1000)
    private String message;

    @Builder
    public BatchExecutionLog(
            BatchType batchType,
            TriggerType triggerType,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            boolean isSuccess,
            int recordCount,
            String message) {
        this.batchType = batchType;
        this.triggerType = triggerType;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.isSuccess = isSuccess;
        this.recordCount = recordCount;
        this.message = message;
    }
}
