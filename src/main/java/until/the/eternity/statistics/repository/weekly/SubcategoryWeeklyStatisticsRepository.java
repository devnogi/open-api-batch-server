package until.the.eternity.statistics.repository.weekly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.weekly.SubcategoryWeeklyStatistics;

import java.util.List;

public interface SubcategoryWeeklyStatisticsRepository
        extends JpaRepository<SubcategoryWeeklyStatistics, Long> {

    /**
     * 서브카테고리별 주간 통계 조회
     *
     * @param subCategory 서브 카테고리
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 조건의 주간 통계 리스트
     */
    @Query(
            "SELECT s FROM SubcategoryWeeklyStatistics s WHERE s.itemSubCategory = :subCategory "
                    + "AND s.weekStartDate BETWEEN :startDate AND :endDate "
                    + "ORDER BY s.year ASC, s.weekNumber ASC")
    List<SubcategoryWeeklyStatistics> findBySubcategoryAndDateRange(
            @Param("subCategory") String subCategory,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    /** 전주의 ItemWeeklyStatistics 데이터를 기반으로 서브카테고리별 주간 통계를 집계하여 upsert */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO subcategory_weekly_statistics (
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
                        ah.item_sub_category,
                        iws.year,
                        iws.week_number,
                        iws.week_start_date,
                        MIN(iws.min_price) AS min_price,
                        MAX(iws.max_price) AS max_price,
                        AVG(iws.avg_price) AS avg_price,
                        SUM(iws.total_volume) AS total_volume,
                        SUM(iws.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM item_weekly_statistics iws
                    INNER JOIN auction_history ah ON iws.item_name = ah.item_name and iws.item_sub_category = ah.item_sub_category
                    WHERE iws.week_start_date = DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)
                    GROUP BY ah.item_sub_category, iws.year, iws.week_number, iws.week_start_date
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
