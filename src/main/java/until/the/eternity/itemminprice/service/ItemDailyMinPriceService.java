package until.the.eternity.itemminprice.service;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemminprice.repository.ItemDailyMinPriceRepository;

/** 일간 최저가 upsert 배치를 실행하는 서비스. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDailyMinPriceService {

    private final ItemDailyMinPriceRepository itemDailyMinPriceRepository;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    @Transactional
    @Operation(
            summary = "item_daily_min_price 5분 주기 최신화",
            description = "auction_history에서 오늘 거래된 최저가를 upsert")
    public void upsertTodayMinPricesEvery5Min() {
        long start = System.currentTimeMillis();
        itemDailyMinPriceRepository.upsertTodayMinPrices();
        log.info(
                "[ItemDailyMinPrice] 5‑min upsert completed in {} ms",
                System.currentTimeMillis() - start);
    }
}
