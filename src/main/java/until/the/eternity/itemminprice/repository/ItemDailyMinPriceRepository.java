package until.the.eternity.itemminprice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemminprice.domain.entity.ItemDailyMinPrice;

public interface ItemDailyMinPriceRepository extends JpaRepository<ItemDailyMinPrice, Long> {

    /**
     * 오늘(서버 타임존 기준) 거래된 각 아이템의 최저가를 item_daily_min_price 테이블에 upsert. 실시간 API가 아니라 9시 전
     * 정보가 들어온다... 최저가 갱신도 9시간 전을 기준으로 한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value =
                    """
                    INSERT INTO item_daily_min_price (
                        item_name,
                        min_price,
                        date_auction_buy,
                        updated_at
                    )
                    SELECT
                        ah.item_name,
                        MIN(ah.auction_price_per_unit) AS current_min_price,
                        DATE(DATE_SUB(NOW(), INTERVAL 9 HOUR)),
                        CURRENT_TIMESTAMP
                    FROM auction_history ah
                    WHERE DATE(ah.date_auction_buy) = DATE(DATE_SUB(NOW(), INTERVAL 9 HOUR))
                    GROUP BY ah.item_name
                    ON DUPLICATE KEY UPDATE
                                         min_price = VALUES(min_price),
                                         updated_at = CURRENT_TIMESTAMP;
        """,
            nativeQuery = true)
    void upsertTodayMinPrices();
}
