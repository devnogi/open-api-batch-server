package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
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

    /** 탑카테고리별 주간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<TopCategoryWeeklyStatisticsResponse> findAll(Pageable pageable) {
        Page<TopCategoryWeeklyStatistics> page = repository.findAll(pageable);
        Page<TopCategoryWeeklyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 탑카테고리별 주간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public TopCategoryWeeklyStatisticsResponse findById(Long id) {
        TopCategoryWeeklyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "TopCategoryWeeklyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
