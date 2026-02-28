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
import until.the.eternity.statistics.domain.entity.weekly.SubcategoryWeeklyStatistics;
import until.the.eternity.statistics.domain.mapper.SubcategoryWeeklyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryWeeklyStatisticsResponse;
import until.the.eternity.statistics.repository.weekly.SubcategoryWeeklyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubcategoryWeeklyStatisticsService {

    private final SubcategoryWeeklyStatisticsRepository repository;
    private final SubcategoryWeeklyStatisticsMapper mapper;

    /** 서브카테고리별 주간 통계 조회 (subCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_SUBCATEGORY_WEEKLY,
            key = "(#topCategory ?: '') + ':' + (#subCategory ?: '') + ':' + #startDate + ':' + #endDate")
    @Transactional(readOnly = true)
    public List<SubcategoryWeeklyStatisticsResponse> search(
            String topCategory,
            String subCategory,
            LocalDate startDate,
            LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        List<SubcategoryWeeklyStatistics> results =
                repository.findBySubcategoryAndDateRange(subCategory, startDate, endDate);

        return results.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
