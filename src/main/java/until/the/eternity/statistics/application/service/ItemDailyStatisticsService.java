package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDailyStatisticsService {

    private final ItemDailyStatisticsRepository repository;

    /** 아이템별 일간 통계 조회 (itemName, subCategory, topCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_ITEM_DAILY,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildStatisticsItemKey(#itemName, #subCategory, #topCategory, #startDate, #endDate)",
            sync = true)
    @Transactional(readOnly = true)
    public List<ItemDailyStatisticsResponse> search(
            String itemName,
            String subCategory,
            String topCategory,
            LocalDate startDate,
            LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        return repository.findByItemAndDateRange(
                itemName, subCategory, topCategory, startDate, endDate);
    }
}
