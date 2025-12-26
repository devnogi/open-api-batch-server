package until.the.eternity.statistics.repository.weekly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;

public interface ItemWeeklyStatisticsRepository extends JpaRepository<ItemWeeklyStatistics, Long> {

    /** 전주(지난 주 월~일)의 ItemDailyStatistics 데이터를 기반으로 아이템별 주간 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_weekly_statistics (
                        item_name,
                        year,
                        week_number,
                        week_start_date,
                        min_price,
                        max_price,
                        avg_price,
                        total_volume,
                        total_quantity,
                        created_at,
                        updated_at
                    )
                    SELECT
                        ids.item_name,
                        YEAR(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)) AS year,
                        WEEK(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY), 1) AS week_number,
                        DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY) AS week_start_date,
                        MIN(ids.min_price) AS min_price,
                        MAX(ids.max_price) AS max_price,
                        AVG(ids.avg_price) AS avg_price,
                        SUM(ids.total_volume) AS total_volume,
                        SUM(ids.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM item_daily_statistics ids
                    WHERE ids.date_auction_buy >= DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)
                      AND ids.date_auction_buy < DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
                    GROUP BY ids.item_name
                    ON DUPLICATE KEY UPDATE
                        min_price = VALUES(min_price),
                        max_price = VALUES(max_price),
                        avg_price = VALUES(avg_price),
                        total_volume = VALUES(total_volume),
                        total_quantity = VALUES(total_quantity),
                        updated_at = CURRENT_TIMESTAMP;
                    """,
            nativeQuery = true)
    void upsertWeeklyStatistics();
}
