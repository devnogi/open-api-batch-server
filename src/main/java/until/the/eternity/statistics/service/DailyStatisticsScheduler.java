package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatisticsScheduler {

    private final DailyStatisticsService dailyStatisticsService;

    /** 매일 새벽 일간 통계 계산 및 저장 기본 cron: 매일 새벽 3시 (변경 가능) */
    @Scheduled(cron = "${statistics.daily.cron:0 5 * * * *}", zone = "Asia/Seoul")
    public void scheduleDailyStatistics() {
        log.info("[Daily Statistics Scheduler] Starting scheduled task...");
        long start = System.currentTimeMillis();

        try {
            dailyStatisticsService.calculateAndSaveDailyStatistics();
            log.info(
                    "[Daily Statistics Scheduler] Scheduled task completed successfully in {} ms",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error(
                    "[Daily Statistics Scheduler] Error occurred during scheduled task: {}",
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
