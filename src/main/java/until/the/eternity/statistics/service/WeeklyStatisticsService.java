package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.statistics.repository.weekly.ItemWeeklyStatisticsRepository;
import until.the.eternity.statistics.repository.weekly.SubcategoryWeeklyStatisticsRepository;
import until.the.eternity.statistics.repository.weekly.TopCategoryWeeklyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatisticsService {

    private final ItemWeeklyStatisticsRepository itemWeeklyStatisticsRepository;
    private final SubcategoryWeeklyStatisticsRepository subcategoryWeeklyStatisticsRepository;
    private final TopCategoryWeeklyStatisticsRepository topCategoryWeeklyStatisticsRepository;

    /**
     * 전주(지난 주 월~일)의 일간 통계를 기반으로 주간 통계를 계산하여 저장 순서: ItemDaily → ItemWeekly → SubcategoryWeekly →
     * TopCategoryWeekly
     *
     * <p>주간 통계 계산 완료 후 주간 통계 캐시 + 주간 랭킹 캐시를 무효화한다.
     */
    @Caching(
            evict = {
                // 주간 통계 캐시
                @CacheEvict(cacheNames = CacheNames.STATISTICS_ITEM_WEEKLY, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_SUBCATEGORY_WEEKLY,
                        allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.STATISTICS_TOPCATEGORY_WEEKLY,
                        allEntries = true),
                // 주간 기준 랭킹 캐시 (item_weekly_statistics 기반)
                @CacheEvict(cacheNames = CacheNames.RANKING_PRICE_WEEK_HIGHEST, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.RANKING_VOLUME_WEEK_POPULAR, allEntries = true),
            })
    @Transactional
    public void calculateAndSaveWeeklyStatistics() {
        log.info("[Weekly Statistics] Starting weekly statistics calculation...");

        long start = System.currentTimeMillis();

        // 1. ItemDailyStatistics → ItemWeeklyStatistics
        log.info("[Weekly Statistics] Step 1/3: Calculating item weekly statistics...");
        itemWeeklyStatisticsRepository.upsertWeeklyStatistics();
        log.info(
                "[Weekly Statistics] Step 1/3 completed in {} ms",
                System.currentTimeMillis() - start);

        // 2. ItemWeeklyStatistics → SubcategoryWeeklyStatistics
        log.info("[Weekly Statistics] Step 2/3: Calculating subcategory weekly statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryWeeklyStatisticsRepository.upsertWeeklyStatistics();
        log.info(
                "[Weekly Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryWeeklyStatistics → TopCategoryWeeklyStatistics
        log.info("[Weekly Statistics] Step 3/3: Calculating top category weekly statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryWeeklyStatisticsRepository.upsertWeeklyStatistics();
        log.info(
                "[Weekly Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Weekly Statistics] All weekly statistics calculated successfully in {} ms",
                System.currentTimeMillis() - start);
    }
}
