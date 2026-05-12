package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import until.the.eternity.config.CacheNames;
import until.the.eternity.ranking.application.service.VolumeRankingService;
import until.the.eternity.ranking.util.RankingConstants;
import until.the.eternity.statistics.application.service.ItemDailyStatisticsService;
import until.the.eternity.statistics.application.service.TopCategoryDailyStatisticsService;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;
import until.the.eternity.statistics.repository.daily.SubcategoryDailyStatisticsRepository;
import until.the.eternity.statistics.repository.daily.TopCategoryDailyStatisticsRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatisticsService {

    @Qualifier("applicationTaskExecutor")
    private final TaskExecutor taskExecutor;

    private final ItemDailyStatisticsRepository itemDailyStatisticsRepository;
    private final SubcategoryDailyStatisticsRepository subcategoryDailyStatisticsRepository;
    private final TopCategoryDailyStatisticsRepository topCategoryDailyStatisticsRepository;
    private final ItemDailyStatisticsService itemDailyStatisticsReadService;
    private final TopCategoryDailyStatisticsService topCategoryDailyStatisticsReadService;
    private final VolumeRankingService volumeRankingService;

    private record DailyStatisticsWarmupTarget(
            String itemName, String topCategory, String subCategory) {}

    /**
     * 당일의 경매 거래 내역을 기반으로 일간 통계를 업데이트 AuctionHistoryScheduler가 실행될 때마다 호출되어 당일 통계만 갱신 순서:
     * auction_history → ItemDaily → SubcategoryDaily → TopCategoryDaily
     *
     * <p>통계 계산 완료 후 일간 통계 캐시 + 오늘/카테고리 기반 랭킹 캐시를 무효화한다.
     */
    @Caching(
            evict = {
                // 일간 통계 캐시
                @CacheEvict(cacheNames = CacheNames.STATISTICS_ITEM_DAILY, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_SUBCATEGORY_DAILY,
                        allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_TOPCATEGORY_DAILY,
                        allEntries = true),
                // 오늘 기준 랭킹 캐시 (item_daily_statistics 기반)
                @CacheEvict(cacheNames = CacheNames.RANKING_PRICE_TODAY_HIGHEST, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_PRICE_TODAY_VOLUME, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.RANKING_VOLUME_TODAY_POPULAR,
                        allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_PRICE_SURGE, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_PRICE_DROP, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_VOLUME_SURGE, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CATEGORY_HIGHEST, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CATEGORY_POPULAR, allEntries = true),
            })
    @Transactional
    public void calculateAndSaveCurrentDayStatistics() {
        log.info("[Current Day Statistics] Starting current day statistics calculation...");

        long start = System.currentTimeMillis();

        // 1. auction_history → ItemDailyStatistics (당일)
        log.info("[Current Day Statistics] Step 1/3: Calculating item daily statistics...");
        itemDailyStatisticsRepository.upsertCurrentDayStatistics();
        log.info(
                "[Current Day Statistics] Step 1/3 completed in {} ms",
                System.currentTimeMillis() - start);

        // 2. ItemDailyStatistics → SubcategoryDailyStatistics (당일)
        log.info("[Current Day Statistics] Step 2/3: Calculating subcategory daily statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryDailyStatisticsRepository.upsertCurrentDayStatistics();
        log.info(
                "[Current Day Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryDailyStatistics → TopCategoryDailyStatistics (당일)
        log.info("[Current Day Statistics] Step 3/3: Calculating top category daily statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryDailyStatisticsRepository.upsertCurrentDayStatistics();
        log.info(
                "[Current Day Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Current Day Statistics] All current day statistics calculated successfully in {} ms",
                System.currentTimeMillis() - start);
        scheduleReadCacheWarmup("current-day");
    }

    /**
     * 전날의 경매 거래 내역을 기반으로 일간 통계를 최종 확정 매일 새벽 한 번 실행되어 전날 23시대 거래까지 포함한 통계를 완성 순서: auction_history →
     * ItemDaily → SubcategoryDaily → TopCategoryDaily
     *
     * <p>전날 통계 확정 후 변동률 랭킹(어제 대비 오늘)도 함께 무효화한다.
     */
    @Caching(
            evict = {
                @CacheEvict(cacheNames = CacheNames.STATISTICS_ITEM_DAILY, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_SUBCATEGORY_DAILY,
                        allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_TOPCATEGORY_DAILY,
                        allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_PRICE_TODAY_HIGHEST, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_PRICE_TODAY_VOLUME, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.RANKING_VOLUME_TODAY_POPULAR,
                        allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_PRICE_SURGE, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_PRICE_DROP, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CHANGE_VOLUME_SURGE, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CATEGORY_HIGHEST, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_CATEGORY_POPULAR, allEntries = true),
            })
    @Transactional
    public void calculateAndSavePreviousDayStatistics() {
        log.info("[Previous Day Statistics] Starting previous day statistics finalization...");

        long start = System.currentTimeMillis();

        // 1. auction_history → ItemDailyStatistics (전날)
        log.info("[Previous Day Statistics] Step 1/3: Finalizing item daily statistics...");
        itemDailyStatisticsRepository.upsertPreviousDayStatistics();
        log.info(
                "[Previous Day Statistics] Step 1/3 completed in {} ms",
                System.currentTimeMillis() - start);

        // 2. ItemDailyStatistics → SubcategoryDailyStatistics (전날)
        log.info("[Previous Day Statistics] Step 2/3: Finalizing subcategory daily statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryDailyStatisticsRepository.upsertPreviousDayStatistics();
        log.info(
                "[Previous Day Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryDailyStatistics → TopCategoryDailyStatistics (전날)
        log.info("[Previous Day Statistics] Step 3/3: Finalizing top category daily statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryDailyStatisticsRepository.upsertPreviousDayStatistics();
        log.info(
                "[Previous Day Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Previous Day Statistics] All previous day statistics finalized successfully in {} ms",
                System.currentTimeMillis() - start);
        scheduleReadCacheWarmup("previous-day");
    }

    private void scheduleReadCacheWarmup(String reason) {
        Runnable warmup = () -> warmReadCaches(reason);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            taskExecutor.execute(warmup);
                        }
                    });
            return;
        }
        taskExecutor.execute(warmup);
    }

    private void warmReadCaches(String reason) {
        try {
            LocalDate end = LocalDate.now();
            LocalDate dailyStart = end.minusDays(14);

            for (DailyStatisticsWarmupTarget target : dailyStatisticsWarmupTargets()) {
                itemDailyStatisticsReadService.search(
                        target.itemName(),
                        target.subCategory(),
                        target.topCategory(),
                        dailyStart,
                        end);
                topCategoryDailyStatisticsReadService.search(target.topCategory(), dailyStart, end);
            }

            volumeRankingService.getTodayPopular(20);
            volumeRankingService.getTodayPopular(RankingConstants.DEFAULT_LIMIT);

            log.info("[Daily Statistics] Read cache warmup completed after {}", reason);
        } catch (Exception e) {
            log.warn(
                    "[Daily Statistics] Read cache warmup failed after {}: {}",
                    reason,
                    e.getMessage(),
                    e);
        }
    }

    private List<DailyStatisticsWarmupTarget> dailyStatisticsWarmupTargets() {
        return List.of(
                new DailyStatisticsWarmupTarget("향기로운 꿀 우유", "기타", "기타"),
                new DailyStatisticsWarmupTarget("축복의 포션", "소모품", "포션"));
    }
}
