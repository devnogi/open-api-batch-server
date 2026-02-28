package until.the.eternity.auctionhistory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.common.enums.SortField;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.config.CacheNames;

/**
 * 경매 거래 내역 캐시 워밍업 서비스.
 *
 * <p>AuctionHistoryScheduler 배치 완료 후 호출되어 다음 작업을 수행한다.
 *
 * <ol>
 *   <li>auction-history:search 캐시 전체 무효화
 *   <li>auction_history 기반 역대 랭킹 캐시 무효화 (ALLTIME_HIGHEST, ALLTIME_MONTH_VOLUME)
 *   <li>page 1~2 × size 20 × sortField 3종 × direction 2종 = 12가지 조합 캐시 워밍
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionHistoryCacheWarmupService {

    private static final int WARMUP_SIZE = 20;
    private static final int WARMUP_MAX_PAGE = 2;

    private final AuctionHistoryService auctionHistoryService;
    private final CacheManager cacheManager;

    /**
     * 캐시 무효화 후 기본 30가지 조합을 선제적으로 워밍한다.
     *
     * <p>빈 검색 조건(필터 없음) 기준으로 워밍하므로, 단순 목록 조회 요청에 즉시 캐시 히트가 발생한다.
     */
    public void evictAndWarm() {
        evictCaches();
        warmup();
    }

    private void evictCaches() {
        clearCache(CacheNames.AUCTION_HISTORY_SEARCH);
        // auction_history 전체를 직접 쿼리하는 역대 랭킹도 함께 무효화
        clearCache(CacheNames.RANKING_ALLTIME_HIGHEST);
        clearCache(CacheNames.RANKING_ALLTIME_MONTH_VOLUME);
        log.info("[Cache Warmup] Evicted: {}, {}, {}",
                CacheNames.AUCTION_HISTORY_SEARCH,
                CacheNames.RANKING_ALLTIME_HIGHEST,
                CacheNames.RANKING_ALLTIME_MONTH_VOLUME);
    }

    private void warmup() {
        // 옵션 필터가 모두 null인 빈 검색 조건 (캐싱 condition 충족)
        AuctionHistorySearchRequest emptyRequest =
                new AuctionHistorySearchRequest(
                        null, null, null, null, null, null, null, null, null);

        int successCount = 0;
        int failCount = 0;

        for (int page = 1; page <= WARMUP_MAX_PAGE; page++) {
            for (SortField sortField : SortField.values()) {
                for (SortDirection direction : SortDirection.values()) {
                    PageRequestDto pageRequest =
                            new PageRequestDto(page, WARMUP_SIZE, sortField, direction);
                    try {
                        auctionHistoryService.search(emptyRequest, pageRequest);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        log.warn(
                                "[Cache Warmup] Failed: page={}, sortField={}, direction={}",
                                page,
                                sortField,
                                direction,
                                e);
                    }
                }
            }
        }

        log.info(
                "[Cache Warmup] Completed: success={}, fail={} (total={})",
                successCount,
                failCount,
                WARMUP_MAX_PAGE * SortField.values().length * SortDirection.values().length);
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
