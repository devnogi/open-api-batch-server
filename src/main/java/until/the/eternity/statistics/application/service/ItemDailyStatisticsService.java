package until.the.eternity.statistics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
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

    /** 아이템별 일간 통계 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public PageResponseDto<ItemDailyStatisticsResponse> findAll(Pageable pageable) {
        Page<ItemDailyStatistics> page = repository.findAll(pageable);
        Page<ItemDailyStatisticsResponse> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /** 아이템별 일간 통계 ID로 단건 조회 */
    @Transactional(readOnly = true)
    public ItemDailyStatisticsResponse findById(Long id) {
        ItemDailyStatistics entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "ItemDailyStatistics not found: " + id));
        return mapper.toDto(entity);
    }
}
