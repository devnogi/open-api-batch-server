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
     * 당일의 경매 거래 내역을 기반으로 일간 통계를 업데이트
     * AuctionHistoryScheduler가 실행될 때마다 호출되어 당일 통계만 갱신
     * 순서: auction_history → ItemDaily → SubcategoryDaily → TopCategoryDaily
     */
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
        log.info(
                "[Current Day Statistics] Step 2/3: Calculating subcategory daily statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryDailyStatisticsRepository.upsertCurrentDayStatistics();
        log.info(
                "[Current Day Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryDailyStatistics → TopCategoryDailyStatistics (당일)
        log.info(
                "[Current Day Statistics] Step 3/3: Calculating top category daily statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryDailyStatisticsRepository.upsertCurrentDayStatistics();
        log.info(
                "[Current Day Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Current Day Statistics] All current day statistics calculated successfully in {} ms",
                System.currentTimeMillis() - start);
    }

    /**
     * 전날의 경매 거래 내역을 기반으로 일간 통계를 최종 확정
     * 매일 새벽 한 번 실행되어 전날 23시대 거래까지 포함한 통계를 완성
     * 순서: auction_history → ItemDaily → SubcategoryDaily → TopCategoryDaily
     */
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
        log.info(
                "[Previous Day Statistics] Step 2/3: Finalizing subcategory daily statistics...");
        long step2Start = System.currentTimeMillis();
        subcategoryDailyStatisticsRepository.upsertPreviousDayStatistics();
        log.info(
                "[Previous Day Statistics] Step 2/3 completed in {} ms",
                System.currentTimeMillis() - step2Start);

        // 3. SubcategoryDailyStatistics → TopCategoryDailyStatistics (전날)
        log.info(
                "[Previous Day Statistics] Step 3/3: Finalizing top category daily statistics...");
        long step3Start = System.currentTimeMillis();
        topCategoryDailyStatisticsRepository.upsertPreviousDayStatistics();
        log.info(
                "[Previous Day Statistics] Step 3/3 completed in {} ms",
                System.currentTimeMillis() - step3Start);

        log.info(
                "[Previous Day Statistics] All previous day statistics finalized successfully in {} ms",
                System.currentTimeMillis() - start);
    }
}
