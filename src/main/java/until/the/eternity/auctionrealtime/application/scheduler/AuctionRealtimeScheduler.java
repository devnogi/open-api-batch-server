package until.the.eternity.auctionrealtime.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.application.service.AuctionRealtimeService;
import until.the.eternity.auctionrealtime.application.service.fetcher.AuctionRealtimeFetcher;
import until.the.eternity.auctionrealtime.application.service.persister.AuctionRealtimePersister;
import until.the.eternity.auctionrealtime.domain.service.fetcher.AuctionRealtimeFetcherPort.FetchResult;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 실시간 경매장 데이터 수집 스케줄러.
 *
 * <p>10분 간격으로 Nexon Open API /auction/list를 호출하여 현재 판매 중인 아이템 정보를 수집한다.
 *
 * <p>각 서브 카테고리별로 전체 데이터를 수집한 뒤, 기존 데이터를 삭제하고 새 데이터로 교체한다. (Full Refresh)
 *
 * <p>Full Refresh 완료 후 auction-realtime:search 캐시 전체를 무효화한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRealtimeScheduler {

    private final AuctionRealtimeService service;
    private final AuctionRealtimeFetcher fetcher;
    private final AuctionRealtimePersister persister;

    @Value("${openapi.auction-realtime.delay-ms:500}")
    private long delayMs;

    /** 10분 간격으로 실행: 0, 10, 20, 30, 40, 50분. */
    @Scheduled(cron = "${openapi.auction-realtime.cron:0 0/10 * * * *}", zone = "Asia/Seoul")
    public void fetchAndSaveAuctionRealtimeAll() {
        log.info("[REALTIME] Starting Auction Realtime scheduler");

        // ItemCategory를 topCategory별로 그룹화
        Map<String, List<ItemCategory>> categoriesByTopCategory =
                Arrays.stream(ItemCategory.values())
                        .collect(
                                Collectors.groupingBy(
                                        ItemCategory::getTopCategory,
                                        LinkedHashMap::new,
                                        Collectors.toList()));

        int totalSavedCount = 0;
        int totalFailedCount = 0;
        List<String> topCategories = new ArrayList<>(categoriesByTopCategory.keySet());

        for (int topIndex = 0; topIndex < topCategories.size(); topIndex++) {
            String topCategory = topCategories.get(topIndex);
            List<ItemCategory> subCategories = categoriesByTopCategory.get(topCategory);

            log.debug("[REALTIME] Processing top category [{}]", topCategory);

            for (int subIndex = 0; subIndex < subCategories.size(); subIndex++) {
                ItemCategory category = subCategories.get(subIndex);
                try {
                    log.debug("[REALTIME] Processing category [{}]", category.getSubCategory());

                    // API 호출 및 전체 데이터 수집
                    FetchResult fetchResult = fetcher.fetch(category);

                    if (fetchResult.items().isEmpty()) {
                        log.debug("[REALTIME] [{}] No data fetched", category.getSubCategory());
                        continue;
                    }

                    // 엔티티 변환
                    List<AuctionRealtimeItem> entities =
                            persister.prepareEntities(fetchResult.items(), category);

                    if (entities.isEmpty()) {
                        continue;
                    }

                    // 기존 데이터 삭제 후 새 데이터 저장 (Full Refresh)
                    service.replaceBySubCategory(category, entities);
                    totalSavedCount += entities.size();

                    // 마지막 서브 카테고리가 아닌 경우에만 delay 적용
                    if (subIndex < subCategories.size() - 1) {
                        log.debug(
                                "[REALTIME] Waiting {}ms before processing next category", delayMs);
                        Thread.sleep(delayMs);
                    }
                } catch (InterruptedException e) {
                    log.error(
                            "[REALTIME] Thread interrupted during delay for category [{}]",
                            category.getSubCategory(),
                            e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error(
                            "[REALTIME] Error during processing category [{}]",
                            category.getSubCategory(),
                            e);
                    totalFailedCount++;
                }
            }

            // 마지막 탑 카테고리가 아닌 경우에만 delay 적용
            if (topIndex < topCategories.size() - 1) {
                try {
                    log.debug(
                            "[REALTIME] Waiting {}ms before processing next top category", delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    log.error(
                            "[REALTIME] Thread interrupted during delay for top category [{}]",
                            topCategory,
                            e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 만료된 아이템 삭제
        int deletedExpired = service.deleteExpiredItems(Instant.now());

        log.info(
                "[REALTIME] Auction Realtime scheduler completed. Total saved: {}, Failed categories: {}, Expired deleted: {}",
                totalSavedCount,
                totalFailedCount,
                deletedExpired);

        // Full Refresh 완료 후 실시간 경매 검색 캐시 전체 무효화
        service.evictSearchCache();
    }
}
