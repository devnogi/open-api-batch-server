package until.the.eternity.auctionrealtime.application.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.mapper.AuctionRealtimeMapper;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.config.CacheNames;

/** 실시간 경매장 데이터 Service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRealtimeService {

    private final AuctionRealtimeItemRepositoryPort repository;
    private final AuctionRealtimeMapper mapper;

    /**
     * 실시간 경매장 아이템을 검색한다.
     *
     * <p>캐시 키: 주요 검색 파라미터 조합. 10분 배치 완료 시 전체 무효화된다.
     *
     * @param requestDto 검색 조건
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @Cacheable(
            cacheNames = CacheNames.AUCTION_REALTIME_SEARCH,
            key =
                    "(#requestDto.itemTopCategory() ?: '_') + ':'"
                            + " + (#requestDto.itemSubCategory() ?: '_') + ':'"
                            + " + (#requestDto.itemName() ?: '_') + ':'"
                            + " + (#requestDto.isExactItemName() ?: false) + ':'"
                            + " + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    @Transactional(readOnly = true)
    public PageResponseDto<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> search(
            AuctionRealtimeSearchRequest requestDto, Pageable pageable) {

        Page<AuctionRealtimeItem> page = repository.search(requestDto, pageable);
        Page<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> dtoPage =
                page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /**
     * ID로 실시간 경매장 아이템을 조회한다.
     *
     * @param id 아이템 ID
     * @return 아이템 상세 정보
     */
    @Transactional(readOnly = true)
    public AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> findByIdOrElseThrow(Long id) {
        AuctionRealtimeItem item =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "AuctionRealtimeItem not found: " + id));
        return mapper.toDto(item);
    }

    /**
     * 해당 카테고리의 기존 데이터를 모두 삭제하고 새 데이터를 저장한다. (Full Refresh)
     *
     * @param category 아이템 카테고리
     * @param entities 저장할 엔티티 리스트
     */
    @Transactional
    public void replaceBySubCategory(ItemCategory category, List<AuctionRealtimeItem> entities) {
        int deleted = repository.deleteBySubCategory(category);
        log.info("[REALTIME] [{}] Deleted {} existing records", category.getSubCategory(), deleted);

        repository.saveAll(entities);
        log.info(
                "[REALTIME] [{}] Saved {} new auction realtime items",
                category.getSubCategory(),
                entities.size());
    }

    /**
     * 만료된 아이템을 삭제한다.
     *
     * @param now 현재 시각
     * @return 삭제된 레코드 수
     */
    @Transactional
    public int deleteExpiredItems(Instant now) {
        int deleted = repository.deleteExpiredItems(now);
        log.info("[REALTIME] Deleted {} expired auction realtime items", deleted);
        return deleted;
    }

    /**
     * 실시간 경매 검색 캐시 전체를 무효화한다.
     *
     * <p>AuctionRealtimeScheduler에서 Full Refresh 완료 후 호출한다.
     */
    @CacheEvict(cacheNames = CacheNames.AUCTION_REALTIME_SEARCH, allEntries = true)
    public void evictSearchCache() {
        log.info("[REALTIME] Cache evicted: {}", CacheNames.AUCTION_REALTIME_SEARCH);
    }
}
