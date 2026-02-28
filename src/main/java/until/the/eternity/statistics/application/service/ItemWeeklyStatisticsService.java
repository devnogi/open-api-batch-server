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
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;
import until.the.eternity.statistics.domain.mapper.ItemWeeklyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemWeeklyStatisticsResponse;
import until.the.eternity.statistics.repository.weekly.ItemWeeklyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemWeeklyStatisticsService {

    private final ItemWeeklyStatisticsRepository repository;
    private final ItemWeeklyStatisticsMapper mapper;

    /** 아이템별 주간 통계 조회 (itemName, subCategory, topCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_ITEM_WEEKLY,
            key =
                    "(#itemName ?: '') + ':'"
                            + " + (#subCategory ?: '') + ':'"
                            + " + (#topCategory ?: '') + ':'"
                            + " + #startDate + ':' + #endDate")
    @Transactional(readOnly = true)
    public List<ItemWeeklyStatisticsResponse> search(
            String itemName,
            String subCategory,
            String topCategory,
            LocalDate startDate,
            LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        List<ItemWeeklyStatistics> results =
                repository.findByItemAndDateRange(
                        itemName, subCategory, topCategory, startDate, endDate);

        return results.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
