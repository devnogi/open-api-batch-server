package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public java.util.List<SubcategoryWeeklyStatisticsResponse> search(
            String topCategory, // topCategory는 파라미터로 받지만 조회에는 사용하지 않음 (DB 구조상)
            String subCategory,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        // 날짜 범위 검증 (최대 4개월)
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        // 조회
        java.util.List<SubcategoryWeeklyStatistics> results =
                repository.findBySubcategoryAndDateRange(subCategory, startDate, endDate);

        // DTO 변환
        return results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }
}
