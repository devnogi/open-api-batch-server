package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.TopCategoryDailyStatistics;

public interface TopCategoryDailyStatisticsRepository
        extends JpaRepository<TopCategoryDailyStatistics, Long> {

    /**
     * 당일의 SubcategoryDailyStatistics 데이터를 기반으로 탑카테고리별 통계를 집계하여 upsert
     * item_daily_statistics 테이블을 사용하여 top_category 정보를 가져옴
     */
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
                        ids.item_top_category,
                        sds.date_auction_buy,
                        MIN(sds.min_price) AS min_price,
                        MAX(sds.max_price) AS max_price,
                        AVG(sds.avg_price) AS avg_price,
                        SUM(sds.total_volume) AS total_volume,
                        SUM(sds.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM subcategory_daily_statistics sds
                    INNER JOIN item_daily_statistics ids ON sds.item_sub_category = ids.item_sub_category
                        AND sds.date_auction_buy = ids.date_auction_buy
                    WHERE sds.date_auction_buy = DATE(NOW())
                    GROUP BY ids.item_top_category, sds.date_auction_buy
                    ON DUPLICATE KEY UPDATE
                        min_price = VALUES(min_price),
                        max_price = VALUES(max_price),
                        avg_price = VALUES(avg_price),
                        total_volume = VALUES(total_volume),
                        total_quantity = VALUES(total_quantity),
                        updated_at = CURRENT_TIMESTAMP;
                    """,
            nativeQuery = true)
    void upsertCurrentDayStatistics();

    /**
     * 전날의 SubcategoryDailyStatistics 데이터를 기반으로 탑카테고리별 통계를 최종 확정
     * item_daily_statistics 테이블을 사용하여 top_category 정보를 가져옴
     */
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
                        ids.item_top_category,
                        sds.date_auction_buy,
                        MIN(sds.min_price) AS min_price,
                        MAX(sds.max_price) AS max_price,
                        AVG(sds.avg_price) AS avg_price,
                        SUM(sds.total_volume) AS total_volume,
                        SUM(sds.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM subcategory_daily_statistics sds
                    INNER JOIN item_daily_statistics ids ON sds.item_sub_category = ids.item_sub_category
                        AND sds.date_auction_buy = ids.date_auction_buy
                    WHERE sds.date_auction_buy = DATE(NOW()) - INTERVAL 1 DAY
                    GROUP BY ids.item_top_category, sds.date_auction_buy
                    ON DUPLICATE KEY UPDATE
                        min_price = VALUES(min_price),
                        max_price = VALUES(max_price),
                        avg_price = VALUES(avg_price),
                        total_volume = VALUES(total_volume),
                        total_quantity = VALUES(total_quantity),
                        updated_at = CURRENT_TIMESTAMP;
                    """,
            nativeQuery = true)
    void upsertPreviousDayStatistics();
}
