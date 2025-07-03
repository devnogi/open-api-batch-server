package until.the.eternity.itemminprice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemDailyMinPriceScheduler {

    private final ItemDailyMinPriceService itemDailyMinPriceService;

    @Scheduled(cron = "0 */2 * * * *", zone = "Asia/Seoul")
    public void scheduleMinPriceUpsert() {
        long start = System.currentTimeMillis();
        itemDailyMinPriceService.upsertTodayMinPrices();
        log.info("[Min Price Scheduler] Upsert completed in {} ms", System.currentTimeMillis() - start);
    }
}