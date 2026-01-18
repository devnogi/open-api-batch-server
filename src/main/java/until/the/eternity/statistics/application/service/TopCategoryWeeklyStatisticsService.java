package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public java.util.List<TopCategoryWeeklyStatisticsResponse> search(
            String topCategory, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // 날짜 범위 검증 (최대 4개월)
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        // 조회
        java.util.List<TopCategoryWeeklyStatistics> results =
                repository.findByTopCategoryAndDateRange(topCategory, startDate, endDate);

        // DTO 변환
        return results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }
}
