package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;

import java.time.LocalDate;
import java.util.List;

public interface SubcategoryDailyStatisticsRepository
        extends JpaRepository<SubcategoryDailyStatistics, Long> {

    /**
     * 서브카테고리별 일간 통계 조회
     *
     * @param subCategory 서브 카테고리
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @return 해당 조건의 일간 통계 리스트
     */
    @Query(
            "SELECT s FROM SubcategoryDailyStatistics s WHERE s.itemSubCategory = :subCategory "
                    + "AND s.dateAuctionBuy BETWEEN :startDate AND :endDate "
                    + "ORDER BY s.dateAuctionBuy ASC")
    List<SubcategoryDailyStatistics> findBySubcategoryAndDateRange(
            @Param("subCategory") String subCategory,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 당일의 ItemDailyStatistics 데이터를 기반으로 서브카테고리별 통계를 집계하여 upsert
     * item_daily_statistics 테이블만 사용하여 효율적으로 집계
     */
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
                        ids.item_sub_category,
                        ids.date_auction_buy,
                        MIN(ids.min_price) AS min_price,
                        MAX(ids.max_price) AS max_price,
                        AVG(ids.avg_price) AS avg_price,
                        SUM(ids.total_volume) AS total_volume,
                        SUM(ids.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM item_daily_statistics ids
                    WHERE ids.date_auction_buy = DATE(NOW())
                    GROUP BY ids.item_sub_category, ids.date_auction_buy
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
     * 전날의 ItemDailyStatistics 데이터를 기반으로 서브카테고리별 통계를 최종 확정
     * item_daily_statistics 테이블만 사용하여 효율적으로 집계
     */
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
                        ids.item_sub_category,
                        ids.date_auction_buy,
                        MIN(ids.min_price) AS min_price,
                        MAX(ids.max_price) AS max_price,
                        AVG(ids.avg_price) AS avg_price,
                        SUM(ids.total_volume) AS total_volume,
                        SUM(ids.total_quantity) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM item_daily_statistics ids
                    WHERE ids.date_auction_buy = DATE(NOW()) - INTERVAL 1 DAY
                    GROUP BY ids.item_sub_category, ids.date_auction_buy
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
