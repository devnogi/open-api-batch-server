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
import until.the.eternity.statistics.domain.entity.weekly.TopCategoryWeeklyStatistics;
import until.the.eternity.statistics.domain.mapper.TopCategoryWeeklyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryWeeklyStatisticsResponse;
import until.the.eternity.statistics.repository.weekly.TopCategoryWeeklyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopCategoryWeeklyStatisticsService {

    private final TopCategoryWeeklyStatisticsRepository repository;
    private final TopCategoryWeeklyStatisticsMapper mapper;

    /** 탑카테고리별 주간 통계 조회 (topCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_TOPCATEGORY_WEEKLY,
            key = "(#topCategory ?: '') + ':' + #startDate + ':' + #endDate")
    @Transactional(readOnly = true)
    public List<TopCategoryWeeklyStatisticsResponse> search(
            String topCategory, LocalDate startDate, LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        List<TopCategoryWeeklyStatistics> results =
                repository.findByTopCategoryAndDateRange(topCategory, startDate, endDate);

        return results.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
