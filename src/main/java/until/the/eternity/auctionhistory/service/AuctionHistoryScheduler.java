package until.the.eternity.auctionhistory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryScheduler {

    private final AuctionHistoryService auctionHistoryService;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "0 0 */3 * * *") // 3시간마다
    public void fetchAndSaveAuctionHistoryAll() {
        for (ItemCategory category : ItemCategory.values()) {
            try {
                auctionHistoryService.fetchAndSaveAuctionHistory(category);
            } catch (Exception e) {
                log.error("Error during processing category [{}]", category.getSubCategory(), e);
            }
            delayBetweenRequests();
        }
    }

    private void delayBetweenRequests() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during delay between requests", e);
        }
    }
}
