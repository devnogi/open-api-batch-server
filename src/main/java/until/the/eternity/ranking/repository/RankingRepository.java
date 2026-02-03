package until.the.eternity.ranking.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;

public interface RankingRepository extends JpaRepository<ItemDailyStatistics, Long> {

    // ===== 가격 랭킹 (Price Ranking) =====

    /** 오늘의 최고가 거래 TOP N (API 1) */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.max_price,
                        i.avg_price,
                        i.total_volume,
                        i.total_quantity,
                        i.date_auction_buy
                    FROM item_daily_statistics i
                    WHERE i.date_auction_buy = CURDATE()
                    ORDER BY i.max_price DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findTodayHighestPrice(@Param("limit") int limit);

    /** 이번 주 최고가 아이템 TOP N (API 2) */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.max_price,
                        i.avg_price,
                        i.total_volume,
                        i.total_quantity,
                        i.week_start_date AS date_auction_buy
                    FROM item_weekly_statistics i
                    WHERE i.year = YEAR(CURDATE())
                      AND i.week_number = WEEK(CURDATE(), 1)
                    ORDER BY i.max_price DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findWeekHighestPrice(@Param("limit") int limit);

    /** 오늘의 최대 거래액 TOP N (API 3) */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.max_price,
                        i.avg_price,
                        i.total_volume,
                        i.total_quantity,
                        i.date_auction_buy
                    FROM item_daily_statistics i
                    WHERE i.date_auction_buy = CURDATE()
                    ORDER BY i.total_volume DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findTodayLargestVolume(@Param("limit") int limit);

    // ===== 거래량 랭킹 (Volume Ranking) =====

    /** 오늘의 인기 아이템 TOP N (API 4) - 거래 수량 기준 */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.total_quantity,
                        i.total_volume,
                        i.avg_price,
                        i.date_auction_buy
                    FROM item_daily_statistics i
                    WHERE i.date_auction_buy = CURDATE()
                    ORDER BY i.total_quantity DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findTodayPopular(@Param("limit") int limit);

    /** 이번 주 인기 아이템 TOP N (API 5) - 거래 수량 기준 */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.total_quantity,
                        i.total_volume,
                        i.avg_price,
                        i.week_start_date AS date_auction_buy
                    FROM item_weekly_statistics i
                    WHERE i.year = YEAR(CURDATE())
                      AND i.week_number = WEEK(CURDATE(), 1)
                    ORDER BY i.total_quantity DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findWeekPopular(@Param("limit") int limit);

    // ===== 가격 변동 랭킹 (Price Change Ranking) =====

    /** 가격 급등 TOP N (API 6) - 어제 대비 오늘 가격 상승률 */
    @Query(
            value =
                    """
                    SELECT
                        t.item_name,
                        t.item_top_category,
                        t.item_sub_category,
                        t.avg_price AS today_avg_price,
                        y.avg_price AS yesterday_avg_price,
                        ROUND(((t.avg_price - y.avg_price) / y.avg_price) * 100, 2) AS change_rate,
                        (t.avg_price - y.avg_price) AS price_change
                    FROM item_daily_statistics t
                    INNER JOIN item_daily_statistics y
                        ON t.item_name = y.item_name
                        AND t.item_top_category = y.item_top_category
                        AND t.item_sub_category = y.item_sub_category
                    WHERE t.date_auction_buy = CURDATE()
                      AND y.date_auction_buy = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                      AND y.avg_price > 0
                    ORDER BY change_rate DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findPriceSurge(@Param("limit") int limit);

    /** 가격 급락 TOP N (API 7) - 어제 대비 오늘 가격 하락률 */
    @Query(
            value =
                    """
                    SELECT
                        t.item_name,
                        t.item_top_category,
                        t.item_sub_category,
                        t.avg_price AS today_avg_price,
                        y.avg_price AS yesterday_avg_price,
                        ROUND(((t.avg_price - y.avg_price) / y.avg_price) * 100, 2) AS change_rate,
                        (t.avg_price - y.avg_price) AS price_change
                    FROM item_daily_statistics t
                    INNER JOIN item_daily_statistics y
                        ON t.item_name = y.item_name
                        AND t.item_top_category = y.item_top_category
                        AND t.item_sub_category = y.item_sub_category
                    WHERE t.date_auction_buy = CURDATE()
                      AND y.date_auction_buy = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                      AND y.avg_price > 0
                    ORDER BY change_rate ASC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findPriceDrop(@Param("limit") int limit);

    /** 거래량 급증 TOP N (API 8) - 어제 대비 오늘 거래량 증가율 */
    @Query(
            value =
                    """
                    SELECT
                        t.item_name,
                        t.item_top_category,
                        t.item_sub_category,
                        t.total_quantity AS today_quantity,
                        y.total_quantity AS yesterday_quantity,
                        ROUND(((t.total_quantity - y.total_quantity) / y.total_quantity) * 100, 2) AS change_rate,
                        (t.total_quantity - y.total_quantity) AS quantity_change
                    FROM item_daily_statistics t
                    INNER JOIN item_daily_statistics y
                        ON t.item_name = y.item_name
                        AND t.item_top_category = y.item_top_category
                        AND t.item_sub_category = y.item_sub_category
                    WHERE t.date_auction_buy = CURDATE()
                      AND y.date_auction_buy = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                      AND y.total_quantity > 0
                    ORDER BY change_rate DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findVolumeSurge(@Param("limit") int limit);

    // ===== 카테고리별 랭킹 (Category Ranking) =====

    /** 카테고리별 최고가 TOP N (API 9) */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.max_price,
                        i.avg_price,
                        i.total_volume,
                        i.total_quantity,
                        i.date_auction_buy
                    FROM item_daily_statistics i
                    WHERE i.date_auction_buy = CURDATE()
                      AND i.item_top_category = :topCategory
                      AND (:subCategory IS NULL OR i.item_sub_category = :subCategory)
                    ORDER BY i.max_price DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findCategoryTopPriced(
            @Param("topCategory") String topCategory,
            @Param("subCategory") String subCategory,
            @Param("limit") int limit);

    /** 카테고리별 인기 아이템 TOP N (API 10) - 거래 수량 기준 */
    @Query(
            value =
                    """
                    SELECT
                        i.item_name,
                        i.item_top_category,
                        i.item_sub_category,
                        i.total_quantity,
                        i.total_volume,
                        i.avg_price,
                        i.date_auction_buy
                    FROM item_daily_statistics i
                    WHERE i.date_auction_buy = CURDATE()
                      AND i.item_top_category = :topCategory
                      AND (:subCategory IS NULL OR i.item_sub_category = :subCategory)
                    ORDER BY i.total_quantity DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findCategoryPopular(
            @Param("topCategory") String topCategory,
            @Param("subCategory") String subCategory,
            @Param("limit") int limit);

    // ===== 역대 기록 랭킹 (All-Time Ranking) =====

    /** 역대 최고가 거래 TOP N (API 11) - 원본 거래 내역 기반 */
    @Query(
            value =
                    """
                    SELECT
                        ah.item_name,
                        ah.item_display_name,
                        ah.item_top_category,
                        ah.item_sub_category,
                        ah.auction_price_per_unit,
                        ah.item_count,
                        (ah.auction_price_per_unit * ah.item_count) AS total_price,
                        ah.date_auction_buy
                    FROM auction_history ah
                    ORDER BY ah.auction_price_per_unit DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findAllTimeHighestPrice(@Param("limit") int limit);

    /** 이번 달 최대 거래액 TOP N (API 12) */
    @Query(
            value =
                    """
                    SELECT
                        ah.item_name,
                        ah.item_display_name,
                        ah.item_top_category,
                        ah.item_sub_category,
                        ah.auction_price_per_unit,
                        ah.item_count,
                        (ah.auction_price_per_unit * ah.item_count) AS total_price,
                        ah.date_auction_buy
                    FROM auction_history ah
                    WHERE YEAR(ah.date_auction_buy) = YEAR(CURDATE())
                      AND MONTH(ah.date_auction_buy) = MONTH(CURDATE())
                    ORDER BY (ah.auction_price_per_unit * ah.item_count) DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findMonthLargestVolume(@Param("limit") int limit);
}
