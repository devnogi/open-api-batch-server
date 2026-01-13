package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.event.AuctionHistorySavedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatisticsScheduler {

    private final DailyStatisticsService dailyStatisticsService;

    /**
     * AuctionHistory 저장 완료 이벤트 수신 시 당일 통계 업데이트
     * AuctionHistoryScheduler가 실행될 때마다 자동으로 호출됨
     */
    @EventListener
    public void onAuctionHistorySaved(AuctionHistorySavedEvent event) {
        log.info(
                "[Daily Statistics Event Listener] Received AuctionHistorySavedEvent - {} records saved at {}",
                event.getSavedCount(),
                event.getEventTime());
        long start = System.currentTimeMillis();

        try {
            dailyStatisticsService.calculateAndSaveCurrentDayStatistics();
            log.info(
                    "[Daily Statistics Event Listener] Current day statistics updated successfully in {} ms",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error(
                    "[Daily Statistics Event Listener] Error occurred while updating current day statistics: {}",
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
