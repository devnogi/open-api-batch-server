package until.the.eternity.itemminprice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemminprice.repository.ItemDailyMinPriceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDailyMinPriceService {

    private final ItemDailyMinPriceRepository itemDailyMinPriceRepository;

    @Transactional
    public void upsertTodayMinPrices() {
        itemDailyMinPriceRepository.upsertTodayMinPrices();
    }
}
