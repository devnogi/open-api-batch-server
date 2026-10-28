package until.the.eternity.statistics.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.TopCategoryDailyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopCategoryDailyStatisticsService {

    private final TopCategoryDailyStatisticsRepository repository;

    /** 탑카테고리별 일간 통계 조회 (topCategory, 날짜 범위) */
    @Cacheable(
            cacheNames = CacheNames.STATISTICS_TOPCATEGORY_DAILY,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildStatisticsTopCategoryKey(#topCategory, #startDate, #endDate)",
            sync = true)
    @Transactional(readOnly = true)
    public List<TopCategoryDailyStatisticsResponse> search(
            String topCategory, LocalDate startDate, LocalDate endDate) {
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        return repository.findByTopCategoryAndDateRange(topCategory, startDate, endDate);
    }
}
