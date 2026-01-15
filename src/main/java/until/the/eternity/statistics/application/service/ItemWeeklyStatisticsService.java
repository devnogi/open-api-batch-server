package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
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
    @Transactional(readOnly = true)
    public java.util.List<ItemWeeklyStatisticsResponse> search(
            String itemName,
            String subCategory,
            String topCategory,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        // 날짜 범위 검증 (최대 4개월)
        until.the.eternity.statistics.util.DateRangeValidator.validateWeeklyDateRange(
                startDate, endDate);

        // 조회
        java.util.List<ItemWeeklyStatistics> results =
                repository.findByItemAndDateRange(
                        itemName, subCategory, topCategory, startDate, endDate);

        // DTO 변환
        return results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }
}
