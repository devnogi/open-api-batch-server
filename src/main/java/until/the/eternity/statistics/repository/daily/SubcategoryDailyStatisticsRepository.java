package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;

public interface SubcategoryDailyStatisticsRepository
        extends JpaRepository<SubcategoryDailyStatistics, Long> {

    /** 전날의 ItemDailyStatistics 데이터를 기반으로 서브카테고리별 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO subcategory_daily_statistics (
                        item_sub_category,
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
                        ah.item_sub_category,
                        ids.date_auction_buy,
                        MIN(ids.min_price) AS min_price,
                        MAX(ids.max_price) AS max_price,
                        AVG(ids.avg_price) AS avg_price,
                        SUM(ids.total_volume) AS total_volume,
                        SUM(ids.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM item_daily_statistics ids
                    INNER JOIN auction_history ah ON ids.item_name = ah.item_name
                        AND DATE(ah.date_auction_buy) = ids.date_auction_buy
                    WHERE ids.date_auction_buy = DATE(DATE_SUB(NOW(), INTERVAL 9 HOUR))
                    GROUP BY ah.item_sub_category, ids.date_auction_buy
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
