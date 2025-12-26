package until.the.eternity.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;
import until.the.eternity.statistics.repository.daily.SubcategoryDailyStatisticsRepository;
import until.the.eternity.statistics.repository.daily.TopCategoryDailyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatisticsService {

    private final ItemDailyStatisticsRepository itemDailyStatisticsRepository;
    private final SubcategoryDailyStatisticsRepository subcategoryDailyStatisticsRepository;
    private final TopCategoryDailyStatisticsRepository topCategoryDailyStatisticsRepository;

    /**
     * 전날의 경매 거래 내역을 기반으로 일간 통계를 계산하여 저장 순서: auction_history → ItemDaily → SubcategoryDaily →
     * TopCategoryDaily
     */
    @Transactional
    public void calculateAndSaveDailyStatistics() {
        log.info("[Daily Statistics] Starting daily statistics calculation...");

        long start = System.currentTimeMillis();

        // 1. auction_history → ItemDailyStatistics
        log.info("[Daily Statistics] Step 1/3: Calculating item daily statistics...");
        itemDailyStatisticsRepository.upsertDailyStatistics();
        log.info(
                "[Daily Statistics] Step 1/3 completed in {} ms",
                System.currentTimeMillis() - start);

        // 2. ItemDailyStatistics → SubcategoryDailyStatistics
        log.info("[Daily Statistics] Step 2/3: Calculating subcategory daily statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryDailyStatisticsRepository.upsertDailyStatistics();
        log.info(
                "[Daily Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryDailyStatistics → TopCategoryDailyStatistics
        log.info("[Daily Statistics] Step 3/3: Calculating top category daily statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryDailyStatisticsRepository.upsertDailyStatistics();
        log.info(
                "[Daily Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Daily Statistics] All daily statistics calculated successfully in {} ms",
                System.currentTimeMillis() - start);
    }
}
