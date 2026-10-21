package until.the.eternity.statistics.repository.daily;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.TopCategoryDailyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse;

public interface TopCategoryDailyStatisticsRepository
        extends JpaRepository<TopCategoryDailyStatistics, Long> {

    /**
     * 탑카테고리별 일간 통계 조회
     *
     * @param topCategory 탑 카테고리
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @return 해당 조건의 일간 통계 리스트
     */
    @Query(
            """
            SELECT new until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse(
                t.id,
                t.itemTopCategory,
                t.dateAuctionBuy,
                t.minPrice,
                t.maxPrice,
                t.avgPrice,
                t.totalVolume,
                t.totalQuantity,
                t.createdAt,
                t.updatedAt
            )
            FROM TopCategoryDailyStatistics t
            WHERE t.itemTopCategory = :topCategory
              AND t.dateAuctionBuy BETWEEN :startDate AND :endDate
            ORDER BY t.dateAuctionBuy ASC
            """)
    List<TopCategoryDailyStatisticsResponse> findByTopCategoryAndDateRange(
            @Param("topCategory") String topCategory,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 당일의 SubcategoryDailyStatistics 데이터를 기반으로 탑카테고리별 통계를 집계하여 upsert item_daily_statistics 테이블을
     * 사용하여 top_category 정보를 가져옴
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
     * 전날의 SubcategoryDailyStatistics 데이터를 기반으로 탑카테고리별 통계를 최종 확정 item_daily_statistics 테이블을 사용하여
     * top_category 정보를 가져옴
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
