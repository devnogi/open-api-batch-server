package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public java.util.List<ItemDailyStatisticsResponse> search(
            String itemName,
            String subCategory,
            String topCategory,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        // 날짜 범위 검증 (최대 30일)
        until.the.eternity.statistics.util.DateRangeValidator.validateDailyDateRange(
                startDate, endDate);

        // 조회
        java.util.List<ItemDailyStatistics> results =
                repository.findByItemAndDateRange(
                        itemName, subCategory, topCategory, startDate, endDate);

        // DTO 변환
        return results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }
}
