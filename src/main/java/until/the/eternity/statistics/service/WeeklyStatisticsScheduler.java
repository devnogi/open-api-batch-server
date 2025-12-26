package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyStatisticsScheduler {

    private final WeeklyStatisticsService weeklyStatisticsService;

    /** 매주 월요일 새벽 주간 통계 계산 및 저장 (전주 데이터 집계) 기본 cron: 매주 월요일 새벽 4시 (변경 가능) */
    @Scheduled(cron = "${statistics.weekly.cron:0 0 4 * * MON}", zone = "Asia/Seoul")
    public void scheduleWeeklyStatistics() {
        log.info("[Weekly Statistics Scheduler] Starting scheduled task...");
        long start = System.currentTimeMillis();

        try {
            weeklyStatisticsService.calculateAndSaveWeeklyStatistics();
            log.info(
                    "[Weekly Statistics Scheduler] Scheduled task completed successfully in {} ms",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error(
                    "[Weekly Statistics Scheduler] Error occurred during scheduled task: {}",
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
