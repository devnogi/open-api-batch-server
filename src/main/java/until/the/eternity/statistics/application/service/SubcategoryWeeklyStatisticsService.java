package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
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

    /** 서브카테고리별 주간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<SubcategoryWeeklyStatisticsResponse> findAll(Pageable pageable) {
        Page<SubcategoryWeeklyStatistics> page = repository.findAll(pageable);
        Page<SubcategoryWeeklyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 서브카테고리별 주간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public SubcategoryWeeklyStatisticsResponse findById(Long id) {
        SubcategoryWeeklyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "SubcategoryWeeklyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
