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

    /** 아이템별 주간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<ItemWeeklyStatisticsResponse> findAll(Pageable pageable) {
        Page<ItemWeeklyStatistics> page = repository.findAll(pageable);
        Page<ItemWeeklyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 아이템별 주간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public ItemWeeklyStatisticsResponse findById(Long id) {
        ItemWeeklyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "ItemWeeklyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
