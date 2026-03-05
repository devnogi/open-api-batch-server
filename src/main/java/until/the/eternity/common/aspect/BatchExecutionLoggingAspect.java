package until.the.eternity.common.aspect;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import until.the.eternity.batchlog.application.service.BatchExecutionLogService;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.domain.enums.TriggerType;
import until.the.eternity.common.annotation.BatchLog;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSyncResponse;

@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class BatchExecutionLoggingAspect {

    private static final int MESSAGE_MAX_LENGTH = 1000;

    private final BatchExecutionLogService batchExecutionLogService;

    @Around("@annotation(batchLog)")
    public Object log(ProceedingJoinPoint pjp, BatchLog batchLog) throws Throwable {
        TriggerType triggerType = detectTriggerType();
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            Object result = pjp.proceed();
            int count = extractCount(result);
            String message = buildSuccessMessage(batchLog.type(), count);
            batchExecutionLogService.saveSuccess(
                    batchLog.type(), triggerType, startedAt, count, message);
            return result;
        } catch (Exception e) {
            String rawMessage = "실패 사유: " + e.getMessage();
            String message =
                    rawMessage.length() > MESSAGE_MAX_LENGTH
                            ? rawMessage.substring(0, MESSAGE_MAX_LENGTH)
                            : rawMessage;
            batchExecutionLogService.saveFailure(batchLog.type(), triggerType, startedAt, message);
            throw e;
        }
    }

    private TriggerType detectTriggerType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            return TriggerType.MANUAL;
        }
        return TriggerType.AUTO;
    }

    private int extractCount(Object result) {
        if (result instanceof Integer i) {
            return i;
        }
        if (result instanceof ItemInfoSyncResponse r) {
            return r.syncedCount();
        }
        return 0;
    }

    private String buildSuccessMessage(BatchType batchType, int count) {
        return switch (batchType) {
            case AUCTION_HISTORY_BATCH -> String.format("경매 내역 총 %,d건 저장 완료", count);
            case ITEM_INFO_SYNC -> String.format("아이템 정보 %,d건 동기화 완료", count);
            case METALWARE_ATTRIBUTE_SYNC -> String.format("세공 능력치 정보 %,d건 동기화 완료", count);
        };
    }
}
