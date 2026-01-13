package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;
import until.the.eternity.statistics.domain.mapper.SubcategoryDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.SubcategoryDailyStatisticsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubcategoryDailyStatisticsService {

    private final SubcategoryDailyStatisticsRepository repository;
    private final SubcategoryDailyStatisticsMapper mapper;

    /** 서브카테고리별 일간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<SubcategoryDailyStatisticsResponse> findAll(Pageable pageable) {
        Page<SubcategoryDailyStatistics> page = repository.findAll(pageable);
        Page<SubcategoryDailyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 서브카테고리별 일간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public SubcategoryDailyStatisticsResponse findById(Long id) {
        SubcategoryDailyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "SubcategoryDailyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
