package until.the.eternity.statistics.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.domain.mapper.ItemDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDailyStatisticsService {

    private final ItemDailyStatisticsRepository repository;
    private final ItemDailyStatisticsMapper mapper;

    /** 아이템별 일간 통계 조회 (itemName, subCategory, topCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_ITEM_DAILY,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildStatisticsItemKey(#itemName, #subCategory, #topCategory, #startDate, #endDate)")
    @Transactional(readOnly = true)
    public List<ItemDailyStatisticsResponse> search(
            String itemName,
            String subCategory,
            String topCategory,
            LocalDate startDate,
            LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        List<ItemDailyStatistics> results =
                repository.findByItemAndDateRange(
                        itemName, subCategory, topCategory, startDate, endDate);

        return results.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
