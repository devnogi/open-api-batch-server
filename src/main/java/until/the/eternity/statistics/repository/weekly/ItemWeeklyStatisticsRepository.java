package until.the.eternity.statistics.repository.weekly;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;
import until.the.eternity.statistics.util.WeekConverter;

public interface ItemWeeklyStatisticsRepository extends JpaRepository<ItemWeeklyStatistics, Long> {

    /**
     * 아이템별 주간 통계 조회
     *
     * @param itemName 아이템 이름
     * @param subCategory 서브 카테고리
     * @param topCategory 탑 카테고리
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 조건의 주간 통계 리스트
     */
    @Query(
            "SELECT i FROM ItemWeeklyStatistics i WHERE i.itemName = :itemName "
                    + "AND i.itemSubCategory = :subCategory AND i.itemTopCategory = :topCategory "
                    + "AND i.weekStartDate BETWEEN :startDate AND :endDate "
                    + "ORDER BY i.year ASC, i.weekNumber ASC")
    List<ItemWeeklyStatistics> findByItemAndDateRange(
            @Param("itemName") String itemName,
            @Param("subCategory") String subCategory,
            @Param("topCategory") String topCategory,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    /** 전주(지난 주 월~일)의 ItemDailyStatistics 데이터를 기반으로 아이템별 주간 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_weekly_statistics (
                        item_name,
                        item_top_category,
                        item_sub_category,
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
                        item_top_category,
                        item_sub_category,
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
                    GROUP BY ids.item_name, item_top_category, item_sub_category
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
