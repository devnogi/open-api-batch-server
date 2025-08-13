package until.the.eternity.auctionhistory.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryService;
import until.the.eternity.auctionhistory.application.service.fetcher.AuctionHistoryFetcher;
import until.the.eternity.auctionhistory.application.service.persister.AuctionHistoryPersister;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryScheduler {

    private final AuctionHistoryService auctionHistoryService;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "${openapi.auction-history.cron}", zone = "Asia/Seoul")
    public void fetchAndSaveAuctionHistoryAll() {
        List<AuctionHistory> newEntities = new ArrayList<>();
        for (ItemCategory category : ItemCategory.values()) {
            try {
                // todo: fake 코드 제거
                List<OpenApiAuctionHistoryResponse> fetchedDtos = fetcher.fetch(category);
                // List<AuctionHistory> entities = persister.saveIfNotExists(fetcher.fetch(category), category);
                // newEntities.add(entities);
            } catch (Exception e) {
                log.error("Error during processing category [{}]", category.getSubCategory(), e);
            }
            auctionHistoryService.fetchAndSaveAuctionHistory(category);
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
