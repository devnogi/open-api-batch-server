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

    /** 탑카테고리별 일간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<TopCategoryDailyStatisticsResponse> findAll(Pageable pageable) {
        Page<TopCategoryDailyStatistics> page = repository.findAll(pageable);
        Page<TopCategoryDailyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 탑카테고리별 일간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public TopCategoryDailyStatisticsResponse findById(Long id) {
        TopCategoryDailyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "TopCategoryDailyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
