package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;

public interface ItemDailyStatisticsRepository extends JpaRepository<ItemDailyStatistics, Long> {

    /** 전날 거래된 각 아이템의 통계를 item_daily_statistics 테이블에 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_daily_statistics (
                        item_name,
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
                        ah.item_name,
                        DATE(DATE_SUB(NOW(), INTERVAL 9 HOUR)) AS date_auction_buy,
                        MIN(ah.auction_price_per_unit) AS min_price,
                        MAX(ah.auction_price_per_unit) AS max_price,
                        AVG(ah.auction_price_per_unit) AS avg_price,
                        SUM(ah.auction_price_per_unit * ah.item_count) AS total_volume,
                        SUM(ah.item_count) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM auction_history ah
                    WHERE DATE(ah.date_auction_buy) = DATE(DATE_SUB(NOW(), INTERVAL 9 HOUR))
                    GROUP BY ah.item_name
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
