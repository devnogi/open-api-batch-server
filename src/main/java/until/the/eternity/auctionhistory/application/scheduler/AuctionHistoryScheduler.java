package until.the.eternity.auctionhistory.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryService;
import until.the.eternity.auctionhistory.application.service.fetcher.AuctionHistoryFetcher;
import until.the.eternity.auctionhistory.application.service.persister.AuctionHistoryPersister;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.event.AuctionHistorySavedEvent;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryScheduler {

    private final AuctionHistoryService service;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "${openapi.auction-history.cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void fetchAndSaveAuctionHistoryAll() {
        // ItemCategory를 topCategory별로 그룹화
        Map<String, List<ItemCategory>> categoriesByTopCategory =
                Arrays.stream(ItemCategory.values())
                        .collect(
                                Collectors.groupingBy(
                                        ItemCategory::getTopCategory,
                                        LinkedHashMap::new,
                                        Collectors.toList()));

        int totalSavedCount = 0;
        List<String> topCategories = new ArrayList<>(categoriesByTopCategory.keySet());

        for (int topIndex = 0; topIndex < topCategories.size(); topIndex++) {
            String topCategory = topCategories.get(topIndex);
            List<ItemCategory> subCategories = categoriesByTopCategory.get(topCategory);
            List<AuctionHistory> newEntities = new ArrayList<>();

            log.debug("> [SCHEDULE] Processing top category [{}]", topCategory);

            for (int subIndex = 0; subIndex < subCategories.size(); subIndex++) {
                ItemCategory category = subCategories.get(subIndex);
                try {
                    log.debug("> [SCHEDULE] Processing category [{}]", category.getSubCategory());
                    List<OpenApiAuctionHistoryResponse> fetchedDtos = fetcher.fetch(category);
                    List<AuctionHistory> entities =
                            persister.filterOutExisting(fetchedDtos, category);
                    newEntities.addAll(entities);

                    // 마지막 서브 카테고리가 아닌 경우에만 delay 적용
                    if (subIndex < subCategories.size() - 1) {
                        log.debug(
                                "> [SCHEDULE] Waiting {}ms before processing next category",
                                delayMs);
                        Thread.sleep(delayMs);
                    }
                } catch (InterruptedException e) {
                    log.error(
                            "> [SCHEDULE] Thread interrupted during delay for category [{}]",
                            category.getSubCategory(),
                            e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error(
                            "> [SCHEDULE] Error during processing category [{}]",
                            category.getSubCategory(),
                            e);
                }
            }

            // Top Category별로 저장
            service.saveAll(newEntities);
            totalSavedCount += newEntities.size();
            log.info(
                    "> [SCHEDULE] Saved [{}] new auction history records for top category [{}]",
                    newEntities.size(),
                    topCategory);

            // 마지막 탑 카테고리가 아닌 경우에만 delay 적용
            if (topIndex < topCategories.size() - 1) {
                try {
                    log.debug(
                            "> [SCHEDULE] Waiting {}ms before processing next top category",
                            delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    log.error(
                            "> [SCHEDULE] Thread interrupted during delay for top category [{}]",
                            topCategory,
                            e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info(
                "> [SCHEDULE] AuctionHistoryScheduler saved [{}] new auction history records complete",
                totalSavedCount);

        // 통계 업데이트를 위한 이벤트 발행
        log.debug("> [SCHEDULE] Publishing AuctionHistorySavedEvent with {} records", totalSavedCount);
        eventPublisher.publishEvent(new AuctionHistorySavedEvent(totalSavedCount));
    }
}
