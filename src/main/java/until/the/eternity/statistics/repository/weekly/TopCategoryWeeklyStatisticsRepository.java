package until.the.eternity.statistics.repository.weekly;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.weekly.TopCategoryWeeklyStatistics;

public interface TopCategoryWeeklyStatisticsRepository
        extends JpaRepository<TopCategoryWeeklyStatistics, Long> {

    /**
     * 탑카테고리별 주간 통계 조회
     *
     * @param topCategory 탑 카테고리
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 조건의 주간 통계 리스트
     */
    @Query(
            "SELECT t FROM TopCategoryWeeklyStatistics t WHERE t.itemTopCategory = :topCategory "
                    + "AND t.weekStartDate BETWEEN :startDate AND :endDate "
                    + "ORDER BY t.weekStartDate ASC")
    List<TopCategoryWeeklyStatistics> findByTopCategoryAndDateRange(
            @Param("topCategory") String topCategory,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    /** 전주의 SubcategoryWeeklyStatistics 데이터를 기반으로 탑카테고리별 주간 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO top_category_weekly_statistics (
                        item_top_category,
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
                        ah.item_top_category,
                        sws.year,
                        sws.week_number,
                        sws.week_start_date,
                        MIN(sws.min_price) AS min_price,
                        MAX(sws.max_price) AS max_price,
                        AVG(sws.avg_price) AS avg_price,
                        SUM(sws.total_volume) AS total_volume,
                        SUM(sws.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM subcategory_weekly_statistics sws
                    INNER JOIN auction_history ah ON sws.item_sub_category = ah.item_sub_category
                    WHERE sws.week_start_date = DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)
                    GROUP BY ah.item_top_category, sws.year, sws.week_number, sws.week_start_date
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
