package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreviousDayStatisticsScheduler {

    private final DailyStatisticsService dailyStatisticsService;

    /**
     * 매일 새벽 전날 통계 최종 확정 기본 cron: 매일 새벽 0시 10분 (AuctionHistoryScheduler 0시 5분 실행 이후) 전날 23시대 거래 내역까지
     * 모두 포함된 최종 통계를 저장
     */
    @Scheduled(cron = "${statistics.previous-day.cron:0 10 0 * * *}", zone = "Asia/Seoul")
    public void schedulePreviousDayStatistics() {
        log.info("[Previous Day Statistics Scheduler] Starting scheduled task...");
        long start = System.currentTimeMillis();

        try {
            dailyStatisticsService.calculateAndSavePreviousDayStatistics();
            log.info(
                    "[Previous Day Statistics Scheduler] Scheduled task completed successfully in {} ms",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error(
                    "[Previous Day Statistics Scheduler] Error occurred during scheduled task: {}",
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
