package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;
import until.the.eternity.statistics.domain.mapper.SubcategoryDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.SubcategoryDailyStatisticsRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubcategoryDailyStatisticsService {

    private final SubcategoryDailyStatisticsRepository repository;
    private final SubcategoryDailyStatisticsMapper mapper;

    /** 서브카테고리별 일간 통계 조회 (subCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_SUBCATEGORY_DAILY,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildStatisticsSubcategoryKey(#subCategory, #startDate, #endDate)")
    @Transactional(readOnly = true)
    public List<SubcategoryDailyStatisticsResponse> search(
            String topCategory, String subCategory, LocalDate startDate, LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        List<SubcategoryDailyStatistics> results =
                repository.findBySubcategoryAndDateRange(subCategory, startDate, endDate);

        return results.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
