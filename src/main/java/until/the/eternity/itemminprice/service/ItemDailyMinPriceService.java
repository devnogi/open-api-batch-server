package until.the.eternity.itemminprice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemminprice.domain.dto.response.ItemDailyMinPriceResponseDto;
import until.the.eternity.itemminprice.domain.entity.ItemDailyMinPrice;
import until.the.eternity.itemminprice.domain.mapper.ItemDailyMinPriceMapper;
import until.the.eternity.itemminprice.repository.ItemDailyMinPriceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDailyMinPriceService {

    private final ItemDailyMinPriceRepository itemDailyMinPriceRepository;
    private final ItemDailyMinPriceMapper itemDailyMinPriceMapper;

    @Transactional
    public void upsertTodayMinPrices() {
        itemDailyMinPriceRepository.upsertTodayMinPrices();
    }

    @Transactional(readOnly = true)
    public List<ItemDailyMinPriceResponseDto> findAll() {
        List<ItemDailyMinPrice> entities = itemDailyMinPriceRepository.findAll();
        return entities.stream().map(itemDailyMinPriceMapper::toDto).collect(Collectors.toList());
    }
}
