package until.the.eternity.itemminprice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemminprice.domain.entity.ItemDailyMinPrice;

public interface ItemDailyMinPriceRepository extends JpaRepository<ItemDailyMinPrice, Long> {

    /** 오늘(서버 타임존 기준) 거래된 각 아이템의 최저가를 item_daily_min_price 테이블에 upsert. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
            INSERT INTO item_daily_min_price (
                item_name,
                min_price,
                date_auction_buy,
                created_at
            )
            SELECT
                ah.item_name,
                MIN(ah.auction_price_per_unit) AS min_price,
                MIN(ah.date_auction_buy)       AS date_auction_buy,
                CURDATE()                      AS created_at
            FROM auction_history ah
            WHERE DATE(ah.date_auction_buy) = CURDATE() - INTERVAL 1 DAY
            GROUP BY ah.item_name
            ON DUPLICATE KEY UPDATE
                min_price = LEAST(item_daily_min_price.min_price, VALUES(min_price)),
                date_auction_buy = IF(
                    VALUES(min_price) < item_daily_min_price.min_price,
                    VALUES(date_auction_buy),
                    item_daily_min_price.date_auction_buy
                );
            """,
            nativeQuery = true)
    void upsertTodayMinPrices();
}
