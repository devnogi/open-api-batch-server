package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.TopCategoryDailyStatistics;

public interface TopCategoryDailyStatisticsRepository
        extends JpaRepository<TopCategoryDailyStatistics, Long> {

    /** 전날의 SubcategoryDailyStatistics 데이터를 기반으로 탑카테고리별 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO top_category_daily_statistics (
                        item_top_category,
                        date_auction_buy,
                        min_price,
                        max_price,
                        avg_price,
                        total_volume,
                        total_quantity,
                        created_at,
                        updated_at
                    )
                    SELECT
                        ah.item_top_category,
                        sds.date_auction_buy,
                        MIN(sds.min_price) AS min_price,
                        MAX(sds.max_price) AS max_price,
                        AVG(sds.avg_price) AS avg_price,
                        SUM(sds.total_volume) AS total_volume,
                        SUM(sds.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM subcategory_daily_statistics sds
                    INNER JOIN auction_history ah ON sds.item_sub_category = ah.item_sub_category
                        AND DATE(ah.date_auction_buy) = sds.date_auction_buy
                    WHERE sds.date_auction_buy = DATE(NOW())
                    GROUP BY ah.item_top_category, sds.date_auction_buy
                    ON DUPLICATE KEY UPDATE
                        min_price = VALUES(min_price),
                        max_price = VALUES(max_price),
                        avg_price = VALUES(avg_price),
                        total_volume = VALUES(total_volume),
                        total_quantity = VALUES(total_quantity),
                        updated_at = CURRENT_TIMESTAMP;
                    """,
            nativeQuery = true)
    void upsertDailyStatistics();
}
