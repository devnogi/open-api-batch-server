package until.the.eternity.auctionrealtime.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.RealtimeSortField;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.config.CacheNames;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRealtimeCacheWarmupService {

    private static final int WARMUP_SIZE = 20;
    private static final int WARMUP_MAX_PAGE = 2;

    private final AuctionRealtimeService auctionRealtimeService;
    private final CacheManager cacheManager;

    public void evictAndWarm() {
        clearCache(CacheNames.AUCTION_REALTIME_SEARCH);
        warmup();
    }

    private void warmup() {
        AuctionRealtimeSearchRequest emptyRequest =
                new AuctionRealtimeSearchRequest(
                        null, null, null, null, null, null, null, null, null);

        int successCount = 0;
        int failCount = 0;

        for (int page = 1; page <= WARMUP_MAX_PAGE; page++) {
            for (RealtimeSortField sortField : RealtimeSortField.values()) {
                for (SortDirection direction : SortDirection.values()) {
                    try {
                        auctionRealtimeService.search(
                                emptyRequest,
                                PageRequest.of(
                                        page - 1,
                                        WARMUP_SIZE,
                                        Sort.by(
                                                direction.toSpringDirection(),
                                                sortField.getFieldName())));
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        log.warn(
                                "[Cache Warmup] Realtime failed: page={}, sortField={}, direction={}",
                                page,
                                sortField,
                                direction,
                                e);
                    }
                }
            }
        }

        log.info(
                "[Cache Warmup] Realtime completed: success={}, fail={} (total={})",
                successCount,
                failCount,
                WARMUP_MAX_PAGE
                        * RealtimeSortField.values().length
                        * SortDirection.values().length);
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
