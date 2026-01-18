package until.the.eternity.statistics.repository.daily;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;

import java.time.LocalDate;
import java.util.List;

public interface ItemDailyStatisticsRepository extends JpaRepository<ItemDailyStatistics, Long> {

    /**
     * 아이템별 일간 통계 조회
     *
     * @param itemName 아이템 이름
     * @param subCategory 서브 카테고리
     * @param topCategory 탑 카테고리
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @return 해당 조건의 일간 통계 리스트
     */
    @Query(
            "SELECT i FROM ItemDailyStatistics i WHERE i.itemName = :itemName "
                    + "AND i.itemSubCategory = :subCategory AND i.itemTopCategory = :topCategory "
                    + "AND i.dateAuctionBuy BETWEEN :startDate AND :endDate "
                    + "ORDER BY i.dateAuctionBuy ASC")
    List<ItemDailyStatistics> findByItemAndDateRange(
            @Param("itemName") String itemName,
            @Param("subCategory") String subCategory,
            @Param("topCategory") String topCategory,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 당일 거래된 각 아이템의 통계를 item_daily_statistics 테이블에 upsert
     * AuctionHistoryScheduler가 실행될 때마다 당일 통계만 업데이트
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_daily_statistics (
                        item_name,
                        item_top_category,
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
                        ah.item_name,
                        ah.item_top_category,
                        ah.item_sub_category,
                        DATE(ah.date_auction_buy) AS date_auction_buy,
                        MIN(ah.auction_price_per_unit) AS min_price,
                        MAX(ah.auction_price_per_unit) AS max_price,
                        AVG(ah.auction_price_per_unit) AS avg_price,
                        SUM(ah.auction_price_per_unit * ah.item_count) AS total_volume,
                        SUM(ah.item_count) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM auction_history ah
                    WHERE DATE(ah.date_auction_buy) = DATE(NOW())
                    GROUP BY ah.item_name, ah.item_top_category, ah.item_sub_category, DATE(ah.date_auction_buy)
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
     * 전날 거래된 각 아이템의 통계를 item_daily_statistics 테이블에 최종 확정
     * 매일 새벽에 한 번 실행되어 전날 23시대 거래 내역까지 포함한 통계를 완성
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_daily_statistics (
                        item_name,
                        item_top_category,
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
                        ah.item_name,
                        ah.item_top_category,
                        ah.item_sub_category,
                        DATE(ah.date_auction_buy) AS date_auction_buy,
                        MIN(ah.auction_price_per_unit) AS min_price,
                        MAX(ah.auction_price_per_unit) AS max_price,
                        AVG(ah.auction_price_per_unit) AS avg_price,
                        SUM(ah.auction_price_per_unit * ah.item_count) AS total_volume,
                        SUM(ah.item_count) AS total_quantity,
                        CURRENT_TIMESTAMP AS created_at,
                        CURRENT_TIMESTAMP AS updated_at
                    FROM auction_history ah
                    WHERE DATE(ah.date_auction_buy) = DATE(NOW()) - INTERVAL 1 DAY
                    GROUP BY ah.item_name, ah.item_top_category, ah.item_sub_category, DATE(ah.date_auction_buy)
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
