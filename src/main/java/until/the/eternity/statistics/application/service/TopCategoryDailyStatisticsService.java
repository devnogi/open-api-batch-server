package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.statistics.domain.entity.daily.TopCategoryDailyStatistics;
import until.the.eternity.statistics.domain.mapper.TopCategoryDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.TopCategoryDailyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopCategoryDailyStatisticsService {

    private final TopCategoryDailyStatisticsRepository repository;
    private final TopCategoryDailyStatisticsMapper mapper;

    /** 탑카테고리별 일간 통계 조회 (topCategory, 날짜 범위) */
    @Transactional(readOnly = true)
    public java.util.List<TopCategoryDailyStatisticsResponse> search(
            String topCategory, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // 날짜 범위 검증 (최대 30일)
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        // 조회
        java.util.List<TopCategoryDailyStatistics> results =
                repository.findByTopCategoryAndDateRange(topCategory, startDate, endDate);

        // DTO 변환
        return results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }
}
